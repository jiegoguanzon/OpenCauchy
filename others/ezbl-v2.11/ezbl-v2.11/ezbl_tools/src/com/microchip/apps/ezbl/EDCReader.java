/**
 * Copyright (C) 2017 Microchip Technology Inc.
 *
 * MICROCHIP SOFTWARE NOTICE AND DISCLAIMER:  You may use this software, and any
 * derivatives created by any person or entity by or on your behalf, exclusively
 * with Microchip's products.  Microchip and its licensors retain all ownership
 * and intellectual property rights in the accompanying software and in all
 * derivatives here to.
 *
 * This software and any accompanying information is for suggestion only.  It
 * does not modify Microchip's standard warranty for its products.  You agree
 * that you are solely responsible for testing the software and determining its
 * suitability.  Microchip has no obligation to modify, test, certify, or
 * support the software.
 *
 * THIS SOFTWARE IS SUPPLIED BY MICROCHIP "AS IS".  NO WARRANTIES, WHETHER
 * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED
 * WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR
 * PURPOSE APPLY TO THIS SOFTWARE, ITS INTERACTION WITH MICROCHIP'S PRODUCTS,
 * COMBINATION WITH ANY OTHER PRODUCTS, OR USE IN ANY APPLICATION.
 *
 * IN NO EVENT, WILL MICROCHIP BE LIABLE, WHETHER IN CONTRACT, WARRANTY, TORT
 * (INCLUDING NEGLIGENCE OR BREACH OF STATUTORY DUTY), STRICT LIABILITY,
 * INDEMNITY, CONTRIBUTION, OR OTHERWISE, FOR ANY INDIRECT, SPECIAL, PUNITIVE,
 * EXEMPLARY, INCIDENTAL OR CONSEQUENTIAL LOSS, DAMAGE, FOR COST OR EXPENSE OF
 * ANY KIND WHATSOEVER RELATED TO THE SOFTWARE, HOWSOEVER CAUSED, EVEN IF
 * MICROCHIP HAS BEEN ADVISED OF THE POSSIBILITY OR THE DAMAGES ARE FORESEEABLE.
 * TO THE FULLEST EXTENT ALLOWABLE BY LAW, MICROCHIP'S TOTAL LIABILITY ON ALL
 * CLAIMS IN ANY WAY RELATED TO THIS SOFTWARE WILL NOT EXCEED THE AMOUNT OF
 * FEES, IF ANY, THAT YOU HAVE PAID DIRECTLY TO MICROCHIP FOR THIS SOFTWARE.
 *
 * MICROCHIP PROVIDES THIS SOFTWARE CONDITIONALLY UPON YOUR ACCEPTANCE OF THESE
 * TERMS.
 */
package com.microchip.apps.ezbl;

import static com.microchip.apps.ezbl.CommandAndBuildState.ReadArgs;
import com.microchip.apps.ezbl.EDCReader.Programming2.*;
import com.microchip.apps.ezbl.EDCReader.Programming2.MemoryRange.MemoryRangeIgnoreAddrComparator;
import static com.microchip.apps.ezbl.EDCReader.Programming2.cloneOp;
import com.microchip.apps.ezbl.EZBLState.CPUClass;
import static com.microchip.apps.ezbl.Multifunction.*;
import static com.microchip.apps.ezbl.Multifunction.StringList;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.CharacterData;


/**
 * EZBL code to facilitate reading MPLAB X Extended Device Characteristics (edc)
 * data base to extract target PIC/dsPIC device specific information.
 *
 * Note: requires crownking.edc.jar and crownking.jar (MPLAB X 4.00-) or
 * crownking.common.jar (MPLAB X 4.01+)
 *
 * @author C12128
 */
public class EDCReader
{
    @SuppressWarnings("unchecked")
    public static void GroupPICPartNumbers(List<String> partNumberList, DeviceProductLines productClasses, TreeMap<String, StringList> productFamilies) // productFamilies is <datasheet_id_string, PartNumbersList>
    {
        for(java.lang.reflect.Field f : productClasses.getClass().getDeclaredFields())
        {
            List<String> classList = null;
            try
            {
                classList = (List<String>)(f.get(productClasses));
            }
            catch(IllegalArgumentException | IllegalAccessException ex)
            {
                Logger.getLogger(EDCReader.class.getName()).log(Level.SEVERE, null, ex);
            }

            if(classList != null)
            {
                Multifunction.Substitute1IfAllPresent(partNumberList, classList, f.getName() + "*");
            }
        }
        for(StringList partFamily : productFamilies.values())
        {
            Multifunction.Substitute1IfAllPresent(partNumberList, partFamily.list, partFamily.topString);
        }

        Collections.sort(partNumberList, new PICNameComparator());
    }


    public static class BitField implements Serializable
    {
        static final long serialVersionUID = 1L;
        String name;            // Name of the bitfield
        String desc;
        EZBL_SFR parentSFR;
        List<String> aliases = new ArrayList<String>();
        int position;           // Starting bit index of the bitfied within the parent SFR
        int width;              // Number of bits in the bitfield
        int sfrBitmask;    // Bitmask of bits occupied by this bitfield with respect to the parent SFR
        boolean isHidden;
    }


    public static class EZBL_SFR implements Comparable<EZBL_SFR>, Serializable
    {
        static final long serialVersionUID = 1L;
        public String name = null;
        public List<String> aliases = new ArrayList<String>();
        public String desc = null;
        public long addr = 0;
        public long endAddr = 0;
        public String srcModule = null;
        public int moduleAddrWidth = 0;
        public int width = 0;
        public int bitsDefined = 0;
        public boolean isHidden = false;
        public boolean isDCR = false;
        public List<BitField> bitfields = new ArrayList<BitField>();
        public TreeMap<String, BitField> bitfieldByName = new TreeMap<>();    // BitField name, BitField
        public TreeMap<Integer, BitField> bitfieldByPos = new TreeMap<>();    // BitField name, BitField
        public Node xNode = null;

        @Override
        public int compareTo(EZBL_SFR s)
        {
            return Objects.equals(this.addr, s.addr) ? 0 : (this.addr > s.addr ? 1 : -1);
        }

        public EZBL_SFR()
        {
        }

        public EZBL_SFR(String sfrCSVFields[], String bitfieldCSVRows[])
        {
            // SFRs table
            //  sfrid INTEGER
            //  parts TEXT
            //  address INTEGER
            //  name TEXT
            //  desc TEXT
            //  parentmodule TEXT
            //  hash TEXT
            int i = 2;
            this.addr = Long.decode(sfrCSVFields[i++]);
            this.name = sfrCSVFields[i++].isEmpty() ? null : TrimQuotes(sfrCSVFields[i - 1]);
            this.desc = sfrCSVFields[i++].isEmpty() ? null : TrimQuotes(sfrCSVFields[i - 1]);
            this.srcModule = sfrCSVFields[i++].isEmpty() ? null : TrimQuotes(sfrCSVFields[i - 1]);

            // Bitfields table
            //  bitfieldid INTEGER
            //  sfrid INTEGER NOT NULL
            //  position INTEGER
            //  width INTEGER
            //  name TEXT
            //  desc TEXT
            //  hidden INTEGER
            for(String bfRow : bitfieldCSVRows)
            {
                i = 1;
                String bfFields[] = SimpleSplit(bfRow, ",");
                if(!bfFields[i++].equals(sfrCSVFields[0]))
                    continue;
                BitField bf = new BitField();
                bf.position = Integer.decode(bfFields[i++]);
                bf.width = Integer.decode(bfFields[i++]);
                bf.name = bfFields[i++].isEmpty() ? null : TrimQuotes(bfFields[i - 1]);
                boolean matchFound = false;
                for(BitField searchBF : this.bitfields)
                {
                    if(searchBF.position == bf.position)
                    {
                        searchBF.aliases.add(bf.name);
                        matchFound = true;
                    }
                    break;
                }
                if(!matchFound)
                {
                    bf.parentSFR = this;
                    bf.desc = bfFields[i++].isEmpty() ? null : TrimQuotes(bfFields[i - 1]);
                    bf.isHidden = !bfFields[i++].equals("0");
                    this.bitfields.add(bf);
                    this.bitfieldByName.put(bf.name, bf);
                    this.bitfieldByPos.put(bf.position, bf);
                    this.bitsDefined |= (((int)Math.pow(2, bf.width)) - 1) << bf.position;
                }
            }
        }

        public BitField findBitfield(String bitFieldName, String parentRegName, String parentModuleName, String desc, Integer position, Integer width, Boolean isHidden, Boolean isDCR)
        {
            for(BitField bf : bitfields)
            {
                if(bitFieldName != null)
                    if(!bf.name.equals(bitFieldName))
                        continue;
                if(parentRegName != null)
                    if(!bf.parentSFR.name.equals(parentRegName))
                        continue;
                if(parentModuleName != null)
                    if(!bf.parentSFR.srcModule.equals(parentModuleName))
                        continue;
                if(desc != null)
                    if(!bf.desc.toLowerCase().contains(desc.toLowerCase()))
                        continue;
                if(position != null)
                    if(bf.position != position)
                        continue;
                if(width != null)
                    if(bf.width != width)
                        continue;
                if(isHidden != null)
                    if(bf.isHidden != isHidden)
                        continue;
                if(isDCR != null)
                    if(bf.parentSFR.isDCR != isDCR)
                        continue;

                return bf;
            }

            return null;
        }

        public String getSFRStructureHashAsString()
        {
            return Multifunction.bytesToHex(getSFRStructureHash());
        }

        public byte[] getSFRStructureHash()
        {
            byte[] computedHash = null;
            MessageDigest hashComputer;

            // Compute SHA-256 hash (of all data up to this point except the syncrhonization fields)
            try
            {
                hashComputer = MessageDigest.getInstance("SHA-256");

                hashComputer.update(this.name.getBytes("UTF-8"));
                hashComputer.update((byte)(this.isDCR ? 0 : 1));
                hashComputer.update((byte)(this.isHidden ? 0 : 1));
                hashComputer.update(Integer.toHexString(this.width).getBytes("UTF-8"));
                hashComputer.update(this.srcModule.getBytes("UTF-8"));

                for(BitField bf : this.bitfields)
                {
                    hashComputer.update(bf.name.getBytes("UTF-8"));
                    hashComputer.update(Integer.toHexString(bf.position).getBytes("UTF-8"));
                    hashComputer.update(Integer.toHexString(bf.width).getBytes("UTF-8"));
                    hashComputer.update((byte)(bf.isHidden ? 0 : 1));
                    for(String alias : bf.aliases)
                    {
                        hashComputer.update(alias.getBytes("UTF-8"));
                    }
                }
            }
            catch(NoSuchAlgorithmException ex)
            {
                throw new RuntimeException("ezbl_tools: Cannot find 'SHA-256' hash algorithm. Make sure your JRE includes SHA-256 support.");
            }
            catch(UnsupportedEncodingException ex)
            {
                throw new RuntimeException("ezbl_tools: Cannot decode string as UTF-8.");
            }

            computedHash = hashComputer.digest();
            return computedHash;
        }
    }


    static class PICDatabase implements Serializable
    {
        static final long serialVersionUID = 1L;
        TreeMap<String, EZBL_SFR> regsByName = new TreeMap<String, EZBL_SFR>();   // <SFR/fuse_name, Register>
        TreeMap<String, PICDevice> partsByName = new TreeMap<String, PICDevice>(); // <PartNumber, regs_and_fuses_on_device>
        TreeMap<String, BitField> bitFields = new TreeMap<String, BitField>();    // <Unique string of SFR_name<bit_field_name>, bitfield definition>

    }


    static class DeviceProductLines implements Serializable
    {
        static final long serialVersionUID = 1L;
        List<String> dsPIC30F = new ArrayList<String>();
        List<String> dsPIC33F = new ArrayList<String>();
        List<String> dsPIC33E = new ArrayList<String>();
        List<String> dsPIC33C = new ArrayList<String>();
        List<String> PIC24FK = new ArrayList<String>();
        List<String> PIC24FJ = new ArrayList<String>();
        List<String> PIC24H = new ArrayList<String>();
        List<String> PIC24E = new ArrayList<String>();
        List<String> generic_16bit = new ArrayList<String>();       // PIC24/dsPIC devices with PSVPAG
        List<String> generic_16bit_da = new ArrayList<String>();    // PIC24 devices with DSRPAG/DSWPAG
        List<String> generic_16bit_ep = new ArrayList<String>();    // PIC24E/dsPIC33E/dsPIC33D devices
        List<String> generic_16dsp_ch = new ArrayList<String>();    // dsPIC33C devices
        List<String> generic_unknown = new ArrayList<String>();     // Shouldn't have any of these, needs implementing if we do
    }


    static class FieldsOnGenericDevice implements Serializable
    {
        static final long serialVersionUID = 1L;
        List<BitFieldParams> generic_16bit = new ArrayList<BitFieldParams>();       // PIC24/dsPIC devices with PSVPAG
        List<BitFieldParams> generic_16bit_da = new ArrayList<BitFieldParams>();    // PIC24 devices with DSRPAG/DSWPAG
        List<BitFieldParams> generic_16bit_ep = new ArrayList<BitFieldParams>();    // PIC24E/dsPIC33E/dsPIC33D devices
        List<BitFieldParams> generic_16dsp_ch = new ArrayList<BitFieldParams>();    // dsPIC33C devices

        public void sortByFieldName()
        {
            Collections.sort(generic_16bit, new BitFieldParamsNameComparator());
            Collections.sort(generic_16bit_da, new BitFieldParamsNameComparator());
            Collections.sort(generic_16bit_ep, new BitFieldParamsNameComparator());
            Collections.sort(generic_16dsp_ch, new BitFieldParamsNameComparator());
        }

        public void sortByAddress()
        {
            Collections.sort(generic_16bit, new BitFieldParamsAddrComparator());
            Collections.sort(generic_16bit_da, new BitFieldParamsAddrComparator());
            Collections.sort(generic_16bit_ep, new BitFieldParamsAddrComparator());
            Collections.sort(generic_16dsp_ch, new BitFieldParamsAddrComparator());
        }

    }


    static class SFRList implements Serializable
    {
        static final long serialVersionUID = 1L;
        List<EZBL_SFR> list = new ArrayList<EZBL_SFR>();
    }


    static class BitFieldList implements Serializable
    {
        static final long serialVersionUID = 1L;
        List<BitField> list = new ArrayList<BitField>();
    }


    public static class DeviceRegs implements Serializable
    {
        static final long serialVersionUID = 1L;
        TreeMap<Long, EZBL_SFR> regsByAddr = new TreeMap<>();
        TreeMap<String, EZBL_SFR> regsByName = new TreeMap<>();      // SFR name, SFR class
        TreeMap<String, SFRList> regsWithBitfield = new TreeMap<>(); // Bitfield name, List of SFRs containing the bitfield
        TreeMap<String, BitField> allBitFieldsInAllRegs = new TreeMap<>();

        public DeviceRegs addAll(DeviceRegs moreRegs)
        {
            if(moreRegs == null)
                return this;

            this.allBitFieldsInAllRegs.putAll(moreRegs.allBitFieldsInAllRegs);
            this.regsByAddr.putAll(moreRegs.regsByAddr);
            this.regsByName.putAll(moreRegs.regsByName);
            this.regsWithBitfield.putAll(moreRegs.regsWithBitfield);

            return this;
        }

        /**
         * @return TreeMap<ParentSFRName, MatchingBitField>
         */
        public TreeMap<String, BitField> findBitfield(String bitFieldName, String parentRegName, String parentModuleName, String desc, Integer position, Integer width, Boolean isHidden, Boolean isDCR)
        {
            TreeMap<String, BitField> ret = new TreeMap<String, BitField>();
            for(EZBL_SFR sfr : regsByAddr.values())
            {
                BitField possibleMatch = sfr.findBitfield(bitFieldName, parentRegName, parentModuleName, desc, position, width, isHidden, isDCR);
                if(possibleMatch != null)
                    ret.put(sfr.name, possibleMatch);
            }
            return ret;
        }

        public boolean isEmpty()
        {
            return this.regsByAddr.isEmpty();
        }
    }


    public static class PICDevice extends DeviceRegs
    {
        static final long serialVersionUID = 1L;
        protected String partNumber = null;               // Full device part number without package or order code suffixes, ex: PIC24FJ1024GB610 or dsPIC33EP512MU810
        String supersetPartNumber = null;       // Full device part number of the device in the family, ex: dsPIC33EP512MU814 or PIC24FJ128GA010
        String partClassName = null;            // Partial part number representing the CPU class, ex: PIC24F, PIC24H, PIC24E, dsPIC30F, dsPIC33F, dsPIC33E, dsPIC33C
        protected String genericClassName = null;         // Compiler generic name, ex: generic_16bit, generic_16bit_da, generic_16bit_ep, or generic_16dsp_ch
        long devID = -1;                        // Device ID number (right justified, all revision ID bits zeroed)
        String datasheetID = null;              // DS number
        String dosID = null;                    // DOS number
        Programming2 prog = null;

        public PICDevice(String partNumber, DeviceRegs regs)    // Fields in regs are referenced, not cloned()
        {
            this.setPartNumber(partNumber);
            this.allBitFieldsInAllRegs = regs.allBitFieldsInAllRegs;
            this.regsByAddr = regs.regsByAddr;
            this.regsByName = regs.regsByName;
            this.regsWithBitfield = regs.regsWithBitfield;
        }

        public PICDevice(String partNumber)
        {
            this.setPartNumber(partNumber);
            this.genericClassName = partNumber.startsWith("dsPIC") ? partNumber.substring(0, "dsPIC30F".length())
                                    : partNumber.startsWith("PIC24") ? partNumber.substring(0, "PIC24F".length())
                                      : partNumber.startsWith("PIC32") ? partNumber.substring(0, "PIC32MM".length()) : partNumber;

        }
        public PICDevice()
        {
        }

        public String getPartNumber()
        {
            return partNumber;
        }
        public void setPartNumber(String partNumber)
        {
            this.partNumber = partNumber;
            this.genericClassName = partNumber.startsWith("dsPIC") ? partNumber.substring(0, "dsPIC30F".length())
                                    : partNumber.startsWith("PIC24") ? partNumber.substring(0, "PIC24F".length())
                                      : partNumber.startsWith("PIC32") ? partNumber.substring(0, "PIC32MM".length()) : "";
        }
    }


    public static class BitFieldParamsNameComparator implements Comparator<BitFieldParams>
    {
        @Override
        public int compare(BitFieldParams x, BitFieldParams y)
        {
            SFRNameComparator c = new SFRNameComparator();
            int ret = c.compare(x.name, y.name) * 2;
            if(x.addr < y.addr)
            {
                ret /= 2;
            }

            return ret;
        }
    }


    // Compares only with respect to the 'addr' field.
    public static class BitFieldParamsAddrComparator implements Comparator<BitFieldParams>
    {
        @Override
        public int compare(BitFieldParams x, BitFieldParams y)
        {
            if(x.addr < y.addr)
            {
                return -1;
            }
            if(x.addr == y.addr)
            {
                return 0;
            }
            return 1;
        }
    }


    static class BitFieldParams implements Comparable<BitFieldParams>, Serializable
    {
        static final long serialVersionUID = 1L;
        boolean isSFR = false;
        boolean isDCR = false;      // Will also be tagged as isSFR = true for Config words
        boolean isBitfield = false;
        boolean isDevice = false;
        boolean isGenericDevice = false;
        boolean isEverything = false;
        String name;                // Name of the Bitfield, SFR, DCR, device, or generic device
        String parentName;          // The parent SFR name when isBitfield == true, or the parent module name when isSFR == true
        long addr;
        int position;
        int length;
        int mask;
        List<String> devsWithMatchingAddr = new ArrayList<String>();
        List<String> devsWithMatchingParentAndPosition = new ArrayList<String>();
        List<String> devsWithMatchingAddrAndParentAndPosition = new ArrayList<String>();
        List<String> devsWithMatchingPosition = new ArrayList<String>();
        List<String> containedByGenericDevs = new ArrayList<String>();

        public void GroupMatchingDevNames(DeviceProductLines productClasses, TreeMap<String, StringList> productFamilies)
        {
            GroupPICPartNumbers(devsWithMatchingAddr, productClasses, productFamilies);
            GroupPICPartNumbers(devsWithMatchingParentAndPosition, productClasses, productFamilies);
            GroupPICPartNumbers(devsWithMatchingAddrAndParentAndPosition, productClasses, productFamilies);
            GroupPICPartNumbers(devsWithMatchingPosition, productClasses, productFamilies);
            GroupPICPartNumbers(containedByGenericDevs, productClasses, productFamilies);
        }

        @Override
        public int compareTo(BitFieldParams s)
        {
            if(this.equals(s))
            {
                return 0;
            }
            if(this.isSFR && s.isSFR)
            {
                if(addr < s.addr)
                {
                    return -1;
                }
                if(addr > s.addr)
                {
                    return 1;
                }
                return name.compareTo(s.name);
            }
            else if(this.isBitfield && s.isBitfield)
            {
                if(name.equals(s.name) && parentName.equals(s.parentName) && (position == s.position) && (length == s.length))
                {
                    return 0;
                }
                if(!name.equals(s.name))
                    return name.compareTo(s.name);
                return 1;
            }
            else if(this.isBitfield)
            {
                return 1;
            }
            else if(this.isSFR)
            {
                return -1;
            }

            if(addr < s.addr)
            {
                return -1;
            }
            if(addr > s.addr)
            {
                return 1;
            }
            if(position < s.position)
            {
                return -1;
            }
            if(position > s.position)
            {
                return 1;
            }
            if(!name.equals(s.name))
                return name.compareTo(s.name);

            return 1;
        }

    }

    static public void GenLEDPatternBoing(String outputFilename)
    {
        byte[] data = new byte[4096];
        int statesPerLED = 4;
        double velocity = 0;                // Current speed and heading
        double position = 8 * statesPerLED; // LED 8, with 2 bits of brightness
        double acceleration = -9.81 * 4;    // LEDs/second^2
        double fps = 250;                   // Framerate (62.5 Hz effective with pseudo dimming)
        double deltaTime = fps / data.length;
        double reboundEfficiency = 0.98;

        for(int i = 0; i < data.length; i += 4)
        {
            velocity += acceleration * deltaTime;
            position += velocity * deltaTime;
            if(position <= 0)
            {
                position = 0;
                velocity = -velocity * reboundEfficiency;
            }
            int mainPos = ((int)(position / statesPerLED));
            data[i] = (byte)(1 << ((int)(position / statesPerLED)));
            data[i + 2] = ((int)((position - 1) / statesPerLED)) == mainPos ? data[i] : 0;
            data[i + 1] = ((int)((position - 2) / statesPerLED)) == mainPos ? data[i] : 0;
            data[i + 3] = ((int)((position - 3) / statesPerLED)) == mainPos ? data[i] : 0;
        }

        Multifunction.UpdateFileIfDataDifferent(outputFilename, data);
    }

    static public Node FindAncestorByNodeType(Node startingNode, String ancestorNodeType) // ancestorNodeName is a string like "edc:SFRDef", not a cname attribute.
    {
        Node ancestor;

        ancestor = startingNode.getParentNode();
        while(true)
        {
            if(ancestor.getNodeName().equals(ancestorNodeType))
            {
                return ancestor;
            }

            ancestor = ancestor.getParentNode();
            if(ancestor == null)
            {
                return null;
            }
        }
    }

    public EDCReader()
    {
    }

    /**
     * Recursively searches a NodeList for nodes and node children for a
     * specific name, returning all found nodes in a List<Node> structure.
     *
     * @param startingNodeList NodeList to enumerate all children and match
     *                         against
     *
     * @param nodeNames        Case sensitive node name(s) to searchTags for
     *
     * @return List<Node> containing all Node elements matching the specified
     *         name.
     *
     * If startingNodeList is null, null is returned.
     *
     * If no matches are found, the returned List<Node> has 0 elements in it.
     */
    static public List<Node> FindChildrenByNodeNames(NodeList startingNodeList, String... nodeNames) // nodeName is a string like "edc:SFRFieldDef" or "edc:SFRFieldDef","edc:AdjustPoint", not a cname attribute.
    {
        if(startingNodeList == null)
            return null;

        List<Node> matchingChildren = new ArrayList<Node>();
        for(int i = 0; i < startingNodeList.getLength(); i++)
        {
            Node item = startingNodeList.item(i);
            for(String name : nodeNames)
            {
                matchingChildren.addAll(FindChildrenByNodeNames(item, name));
            }
        }
        return matchingChildren;
    }

    /**
     * Recursively searches a Node for children nodes of a specific name or
     * names, returning all found nodes in a List<Node> structure.
     *
     * @param startingNode Node to start enumerating all children and match
     *                     against
     *
     * @param nodeNames    Case sensitive node name(s) to searchTags for
     *
     * @return List<Node> containing all Node elements matching the specified
     *         name.
     *
     * If startingNode is null, null is returned.
     *
     * If no matches are found, the returned List<Node> has 0 elements in it.
     */
    static public List<Node> FindChildrenByNodeNames(Node startingNode, String... nodeNames) // childNodeType is a string like "edc:SFRFieldDef" or "edc:SFRFieldDef","edc:AdjustPoint", not a cname attribute.
    {
        if(startingNode == null)
            return null;

        NodeList children;
        Node testNode;
        List<Node> matchingChildren = new ArrayList<Node>();

        if(!startingNode.hasChildNodes())
        {
            return matchingChildren;
        }

        children = startingNode.getChildNodes();
        int numChildren = children.getLength();
        for(int i = 0; i < numChildren; i++)
        {
            testNode = children.item(i);
            if(testNode.getNodeType() != CharacterData.ELEMENT_NODE)
                continue;

            String nodeName = testNode.getNodeName();
            for(String searchName : nodeNames)
            {
                if(nodeName.equals(searchName))
                {
                    matchingChildren.add(testNode);
                }
            }
            matchingChildren.addAll(FindChildrenByNodeNames(testNode, nodeNames));
        }

        return matchingChildren;
    }

    /**
     * Returns RAM SFRs or DCRs (Device Configuration Registers, or "fuses")
     * based on input NodeList input ("edc:SFRDef" and "edc:DCRDef")
     *
     * @param SFRDefNodes A NodeList containing any or all XML nodes to index
     *                    and return in mapped form.
     *
     * @param modes...    edc:SFRMode(edc:id==) values to pase. If none
     *                    specified, only returns the "DS.0" default modes.
     *
     * @return DevRegisters class containing all SFRs and Config registers in
     *         easily accessed map objects.
     *
     * If SFRDefNodes is null, null is returned.
     */
    static public DeviceRegs IndexSFRDCRNodes(NodeList SFRDefNodes, String... modes)
    {
        Node item;
        Node temp;
        Node temp2;
        boolean allModes = false;
        DeviceRegs dev = new DeviceRegs();

        if(SFRDefNodes == null)
            return null;

        if((modes == null) || (modes.length == 0))
        {
            modes = new String[1];
            modes[0] = "DS.0";
        }
        else if((modes.length >= 1) && modes[0].equals("*"))
            allModes = true;

        for(int i = 0; i < SFRDefNodes.getLength(); i++)
        {
            item = SFRDefNodes.item(i);
            if(item.getNodeType() != Document.ELEMENT_NODE)
                continue;

            if(!item.hasAttributes())
                continue;

            if(!item.getNodeName().equals("edc:SFRDef") && !item.getNodeName().equals("edc:DCRDef"))
                continue;

            EZBL_SFR sfr = new EZBL_SFR();
            sfr.name = GetAttr(item, "edc:cname", "");
            sfr.xNode = item;
            sfr.addr = Long.decode(GetAttr(item, "edc:_addr", "-1"));
            sfr.isDCR = !item.getNodeName().equals("edc:SFRDef");
            sfr.isHidden = Boolean.parseBoolean(GetAttr(item, "edc:ishidden", "false"));
            sfr.desc = GetAttr(item, "edc:desc", "");
            sfr.srcModule = GetAttr(item, "edc:_modsrc", "");
            sfr.width = Integer.decode(GetAttr(item, "edc:nzwidth", "16"));
            sfr.moduleAddrWidth = sfr.width / 8;
            sfr.endAddr = sfr.addr + (sfr.width / 8);
            if((sfr.addr < 0x800000) && (sfr.width > 16))
            {
                sfr.endAddr = sfr.addr + (sfr.width / 8 / 3 * 2);
                sfr.moduleAddrWidth = sfr.width / 8 / 3 * 2;
            }
            temp = item.getAttributes().getNamedItem("edc:_begin");
            temp2 = item.getAttributes().getNamedItem("edc:_end");
            if((temp != null) && (temp2 != null))
                sfr.moduleAddrWidth = Integer.decode(temp2.getNodeValue()) - Integer.decode(temp.getNodeValue());

            List<Node> aliases = FindChildrenByNodeNames(item, "edc:AliasList");
            for(int j = 0; j < aliases.size(); j++)
            {
                if(!aliases.get(j).hasAttributes())
                    continue;
                sfr.aliases.add(aliases.get(j).getAttributes().getNamedItem("edc:cname").getNodeValue());
            }

            List<Node> bitfieldNodes;
            if(sfr.isDCR)
            {   // Config word
                bitfieldNodes = FindChildrenByNodeNames(item, "edc:DCRMode", "edc:DCRFieldDef", "edc:AdjustPoint");
            }
            else
            {   // SFR
                bitfieldNodes = FindChildrenByNodeNames(item, "edc:SFRMode", "edc:SFRFieldDef", "edc:AdjustPoint");
                if(!allModes)
                {
                    boolean killNodes = false;  // Kills all the nodes between an edc:SFRMode and the next edc:SFRMode to remove alternate modes from those specified in the search
                    for(int j = 0; j < bitfieldNodes.size(); j++)
                    {
                        Node n = bitfieldNodes.get(j);
                        if(n.getNodeName().equals("edc:SFRMode"))
                        {
                            killNodes = true;
                            for(String m : modes)
                            {
                                String id = GetAttr(n, "edc:id");
                                if((id == null) || id.equals(m))
                                {
                                    killNodes = false;
                                    break;
                                }
                            }
                        }
                        else if(killNodes)
                            bitfieldNodes.remove(j--);
                    }
                }
            }

            // Decode bitfields for this SFR/DCR
            int bitPos = -1;
            for(int j = 0; j < bitfieldNodes.size(); j++)
            {
                Node bfNode = bitfieldNodes.get(j);
                if(bfNode.getNodeType() != Document.ELEMENT_NODE)
                    continue;
                if(!bfNode.hasAttributes())
                    continue;
                if(bfNode.getNodeName().equals("edc:SFRMode") || bfNode.getNodeName().equals("edc:DCRMode"))
                {
                    bitPos = 0;
                    continue;
                }
                if(bfNode.getNodeName().equals("edc:AdjustPoint"))
                {
                    bitPos += Integer.decode(GetAttr(bfNode, "edc:offset", "1"));
                    continue;
                }
                if(bfNode.getNodeName().endsWith("FieldDef"))
                {
                    String localCName = GetAttr(bfNode, "edc:cname");
                    if(localCName != null)
                    {
                        BitField bf = new BitField();
                        bf.name = localCName;
                        bf.parentSFR = sfr;
                        bf.position = bitPos;
                        bf.width = Integer.decode(GetAttr(bfNode, "edc:nzwidth", "16"));
                        bf.sfrBitmask = (((int)(Math.pow(2, bf.width))) - 1) << bf.position;
                        bf.desc = GetAttr(bfNode, "edc:desc", "");
                        bf.isHidden = Boolean.parseBoolean(GetAttr(bfNode, "edc:ishidden", "false"));
                        sfr.bitfieldByName.put(bf.name, bf);
                        sfr.bitfields.add(bf);
                        dev.allBitFieldsInAllRegs.put(bf.name, bf);
                        SFRList sfrsWithThisBitfield = dev.regsWithBitfield.get(bf.name);
                        if(sfrsWithThisBitfield == null)
                            sfrsWithThisBitfield = new SFRList();
                        sfrsWithThisBitfield.list.add(sfr);
                        dev.regsWithBitfield.put(bf.name, sfrsWithThisBitfield);
                        bitPos += bf.width;
                    }
                }
            }
            dev.regsByName.put(sfr.name, sfr);
            dev.regsByAddr.put(sfr.addr, sfr);
        }

        return dev;
    }

    static public DecodedNodeList IndexNodes(NodeList XMLNodes, int childRecurseDepth)
    {
        DecodedNodeList decodedNodes = new DecodedNodeList();
        Node xmlNode;

        for(int i = 0; i < XMLNodes.getLength(); i++)
        {
            xmlNode = XMLNodes.item(i);
            if(xmlNode.getNodeType() != CharacterData.ELEMENT_NODE)
                continue;
            decodedNodes.list.add(new DecodedNode(xmlNode));
            if((childRecurseDepth > 0) && xmlNode.hasChildNodes())
            {
                decodedNodes.putAll(IndexNodes(xmlNode.getChildNodes(), childRecurseDepth - 1));
            }
        }

        return decodedNodes;
    }

    static public LinkedHashMap<String, DecodedNode> IndexNodeChildren(Node xmlNode, boolean recursive) // Node tag name, DocodedNode of the original Node
    {
        if(xmlNode == null)
            return null;

        LinkedHashMap<String, DecodedNode> nodeListMap = new LinkedHashMap<>();
        if(!xmlNode.hasChildNodes())
            return nodeListMap;

        NodeList children = xmlNode.getChildNodes();

        for(int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if(child.getNodeType() != CharacterData.ELEMENT_NODE)
                continue;
            DecodedNode dn = new DecodedNode(child);
            if(recursive)
            {
                if(child.hasChildNodes())
                {
                    dn.children = IndexNodeChildren(child, recursive);
                }
            }
            nodeListMap.put(child.getNodeName(), dn);
        }

        return nodeListMap;
    }

    static public Map<String, EZBL_SFR> IndexSFRNodes(NodeList SFRDefNodes)
    {
        return IndexSFRDCRNodes(SFRDefNodes).regsByName;
    }

    static public Node GetAttrNode(Node XMLNode, String attrName)
    {
        return GetAttrNode(XMLNode, attrName, null);
    }

    static public Node GetAttrNode(Node XMLNode, String attrName, Node defaultValue)
    {
        if(XMLNode == null)
            return defaultValue;

        if(attrName == null)
            attrName = "edc:cname";

        NamedNodeMap nm = XMLNode.getAttributes();
        if(nm == null)
            return defaultValue;

        Node attrNode = nm.getNamedItem(attrName);
        if(attrNode == null)
            return defaultValue;

        return attrNode;
    }

    static public String GetAttr(Node XMLNode, String attrName)
    {
        return GetAttr(XMLNode, attrName, null);
    }

    static public String GetAttr(Node XMLNode, String attrName, String defaultValue)
    {
        if(XMLNode == null)
            return defaultValue;

        if(attrName == null)
            attrName = "edc:cname";

        NamedNodeMap nm = XMLNode.getAttributes();
        if(nm == null)
            return defaultValue;

        Node item = nm.getNamedItem(attrName);
        if(item == null)
            return defaultValue;

        String nodeVal = item.getNodeValue();
        if(nodeVal == null)
        {
            return defaultValue;
        }

        return nodeVal;
    }

    static public String FindAttrib(Document doc, String searchString, String attributeName, String defaultValue)
    {
        if(doc == null)
            return defaultValue;

        Node n = FindNode(doc, searchString);
        if((n == null) || !n.hasAttributes())
            return defaultValue;
        n = n.getAttributes().getNamedItem(attributeName);
        if(n == null)
            return defaultValue;

        return n.getNodeValue();
    }

    static public Long FindAttribAsLong(Document doc, String searchString, String attributeName, Long defaultValue)
    {
        if(doc == null)
            return defaultValue;

        Node n = FindNode(doc, searchString);
        if((n == null) || !n.hasAttributes())
            return defaultValue;
        n = n.getAttributes().getNamedItem(attributeName);
        if(n == null)
            return defaultValue;

        try
        {
            return Long.decode(n.getNodeValue());
        }
        catch(NumberFormatException ex)
        {
            return defaultValue;
        }
    }

    static public Integer FindAttribAsInt(Document doc, String searchString, String attributeName, Integer defaultValue)
    {
        if(doc == null)
            return defaultValue;

        Node n = FindNode(doc, searchString);
        if((n == null) || !n.hasAttributes())
            return defaultValue;
        n = n.getAttributes().getNamedItem(attributeName);
        if(n == null)
            return defaultValue;

        try
        {
            return Integer.decode(n.getNodeValue());
        }
        catch(NumberFormatException ex)
        {
            return defaultValue;
        }
    }

    static public Boolean FindAttribAsBoolean(Document doc, String searchString, String attributeName, Boolean defaultValue)
    {
        if(doc == null)
            return defaultValue;

        Node n = FindNode(doc, searchString);
        if((n == null) || !n.hasAttributes())
            return defaultValue;
        n = n.getAttributes().getNamedItem(attributeName);
        if(n == null)
            return defaultValue;

        return Boolean.parseBoolean(n.getNodeValue());
    }

    // Finds a node given a string such as: "edc:ProgramSpace->edc:ProgramSubspace(edc:partitionmode==single)->edc:CodeSector", returning the edc:CodeSector Node
    // @return null if no Node is located matching the given search tree and attributes
    static public Node FindNode(Document doc, String searchString)
    {
        if(doc == null)
            return null;

        if((searchString == null) || searchString.isEmpty())
            return (Node)doc;

        int nextTokenPos = searchString.indexOf("->");
        String search = (nextTokenPos >= 0) ? searchString.substring(0, nextTokenPos) : searchString;
        int attribsStart = search.indexOf('(');
        if(attribsStart >= 0)
            search = search.substring(0, attribsStart);

        return FindNode(doc.getElementsByTagName(search), searchString);
    }

    // Finds a node given a string such as: "edc:ProgramSpace->edc:ProgramSubspace(edc:partitionmode==single)->edc:CodeSector", returning the edc:CodeSector Node
    // @return null if no Node is located matching the given search tree and attributes
    static public Node FindNode(NodeList startingNode, String searchString)
    {
        if(startingNode == null)
            return null;

        if((searchString == null) || searchString.isEmpty())
            return startingNode.item(0);

        int nextTokenPos = searchString.indexOf("->");
        String search = (nextTokenPos >= 0) ? searchString.substring(0, nextTokenPos) : searchString;

        int attribsStart = search.indexOf('(');
        HashMap<String, String> attribEquals = null;
        if(attribsStart >= 0)
        {
            attribEquals = new HashMap<String, String>();
            String attribSearchString = search.substring(attribsStart + 1, search.length() - 1);
            search = search.substring(0, attribsStart);

            String[] attribSearches = attribSearchString.split("\\,");
            for(int j = 0; j < attribSearches.length; j++)
            {
                int equalIndex = attribSearches[j].indexOf("==");
                if(equalIndex >= 0)
                {
                    attribEquals.put(attribSearches[j].substring(0, equalIndex), attribSearches[j].substring(equalIndex + 2, attribSearches[j].length()));
                }
                else
                    attribEquals.put(attribSearches[j], null);
            }
        }
        // Search for tag name match
        for(int j = 0; j < startingNode.getLength(); j++)
        {
            Node n = startingNode.item(j);
            if(n.getNodeType() != Document.ELEMENT_NODE)
                continue;
            if(!search.equals("*") && !n.getNodeName().equals(search))
                continue;
            if(attribEquals != null)
            {
                if(!n.hasAttributes())
                    continue;
                boolean matched = true;
                for(String attr : attribEquals.keySet())
                {
                    Node attribNode = n.getAttributes().getNamedItem(attr);
                    if(attribNode == null)  // Attribute not on this tag
                    {
                        matched = false;
                        break;
                    }
                    if(attribEquals.get(attr) == null)  // Attribute name matched, no comparison needed, so this is a match
                        continue;
                    if(!attribNode.getNodeValue().equals(attribEquals.get(attr)))
                    {
                        matched = false;
                        break;
                    }
                }
                if(!matched)
                    continue;
            }

            // Matches so far, are we at the end of the search string?
            if(nextTokenPos < 0)
                return n;   // No more search parameters. We found the desired node.

            if(n.hasChildNodes())
            {
                Node ret = FindNode(n.getChildNodes(), searchString.substring(nextTokenPos + 2));
                if(ret != null)
                    return ret; // Found it
            }
        }
        return null;    // No match
    }


    static class SFRAlternates implements Comparable<SFRAlternates>
    {
        Long lowestAddr;
        String name;
        boolean isDCR = false;
        TreeMap<Long, String> addrDevListPairs = new TreeMap<Long, String>();   // SFR address, CSV list of device part number strings that implement this SFR address

        @Override
        public int compareTo(SFRAlternates o)
        {
            if(!o.isDCR && isDCR)
            {
                return 1;
            }
            if(o.isDCR && !isDCR)
            {
                return -1;
            }
            if(this.lowestAddr < o.lowestAddr)
            {
                return -1;
            }
            if((this.lowestAddr.longValue() == o.lowestAddr.longValue()) && this.name.equals(o.name))
            {
                if(this.addrDevListPairs.size() < o.addrDevListPairs.size())
                {
                    return -1;
                }
                if(this.addrDevListPairs.size() == o.addrDevListPairs.size())
                {
                    return 0;
                }
            }
            return 1;
        }

    }


    public static class Programming2 implements Comparable<Programming2>
    {
        static final String tag = "Programming2";
        static final String description = "Parameters needed for flash, config word and other NVM erase/programming";
        String partname;
        String superset;
        long devidaddr = 0x00FF0000;
        long devidmask = 0x0000FFFF;
        long devid;
        long nvmconaddr;
        long nvmkeyaddr;
        long nvmkey1 = 0x55;
        long nvmkey2 = 0xAA;
        int vddmin = 3000;          // millivolts
        int vddmax = 3600;          // millivolts
        boolean flashconfig = true;
        String mclrlvl = "vdd";
        ProgrammingMemories memories = new ProgrammingMemories();
        TreeMap<String, NVMOp> nvmops = new TreeMap<>();
        static final String help = "\nNon-overlapping Address ranges where some kind of NVMOP is allowed or will serve some type of purpose (attempted use of a command at an unsupported address outside of this list with matching supports token will yield undefined results; software should assume a default outcome of correct erase/programming, but must test for device reset or other incorrect state afterwards if supporting attempted programming of unspecified address ranges)."
                                   + "\n@cname          Canonical name for this program space region. Do not define new names unless the hardware genuinely does do something special in the range and no existing programming algorithms can properly support the address range without a new code implenmentation."
                                   + "\n@beginaddr      First program space address that this memory range is defining, inclusive."
                                   + "\n@endaddr        First legal program space address after this memory range. I.e. this is an exclusive end address type."
                                   + "\n@supports       List of subsequent NVMCON commands for erasing and programming the address range. ICSP tools and bootloaders will choose how to access this address range from the list of supported commands."
                                   + "\n@hasecc         [Optional, default=false] Boolean indicating if the ApplyRange contains ECC protection and therefore can trigger erroneous ECC SEC and DED errors if programmed more than once with non all '1's values."
                                   + "\n@otp            [Optional, default=false] Boolean indicating if this memory range can only be programmed once (such as user OTP)."
                                   + "\n"
                                   + "\nNVMCON<NVMOP> opcode encodings and properties"
                                   + "\n@destaddrinc    Destination address space address increment affected by this NVMOP. Ex: 0x2 for flash word/Config byte on 16-bit, 0x4 for flash double word on 16-bit, 0x8 for flash double word on 32-bit PIC32MM, 0x10 for flash quad word on 32-bit PIC32MX/MZ. This is typically used to increment a software pointer in order to iterate over a memory range and hit every location exacty once. This parameter is required for all NVMOPs that can be steered to a particular destination address and is not mass intrinsically defined parallel erase operation. It should be included for opcodes that do have intrinsic target address effects, but which are small, such as FBOOT."
                                   + "\n@destbytes      Destination address space bytes affected (or number of bytes of non-phantom data from input stream that would be consumed to define the whole destination block."
                                   + "\n@nvpop          Value to write to NVMCON to trigger this NVMOP, not including the WR bit itself."
                                   + "\n@nvmopmask      Bit mask defining which bits of edc:nvmop are important to trigger the NVMOP. This value is used by software to avoid destroying user state bits in NVMCON, such as SFTSWP, power down in idle, etc."
                                   + "\n@srctype        0 = transparent latches at the dest address, 1 = mapped program space latches or NVMDATAx registers (ex: 0xFA0000, 0x7FF800 for slave loading via PSV latches, or 0xBF8023B0 for NVMDATA0 on PIC32M), 2 = RAM (bus mastered read from system RAM with clocking and phantom bytes needed; set srcaddr to base address of the NVMSRCADR SFRs), 3 = Packed RAM (bus mastered read from system RAM with clocking, no phantom bytes and reordered bytes for 3 RAM input words per destination double word; set srcaddr to base address of the NVMSRCADR SFRs)."
                                   + "\n@srcaddr        Base address where the NVM controller gets the source data from. Ex: 16-bit devices: \"0\" (transparent latches), \"0xFA0000\", \"0x7FF800\", \"0x0762\" (RAM bus mastered) and on PIC32M: \"0xBF8023B0\" (NVMDATA0) or \"0xBF8023D0\" (NVMSRCADDR for RAM bus mastered)."
                                   + "\n@durationtyp    Data sheet typical execution duration at 25C in nanoseconds. This value is required and is used by software to display non-critical user feedback, such as estimated time remaining for programming completion. If unknown, compute this value assuming no FRC error. i.e: 1/(FRC target frequency) * (number of FRC clocks required by the NVM state machine)."
                                   + "\n@durationmax    Data sheet maximum limit for highest temperature/slowest FRC execution time in nanoseconds, unless the data sheet is assuming something unfavorably abnormal, like having OSCTUN set to the slowest possible user settable frequency. If the abnormal case exists or this value is unknown, compute this value assuming a -7% slower than specified FRC PERIOD (not frequency). i.e: 1/(FRC frequency) * 0.93 * (number of FRC clocks required by the NVM state machine). This is the duration an ICSP programming tool or software should use when doing open loop algorithm continuation (not polling the NVMCON<WR> bit for operation completion) with ordinary production/calibrated devices. It may also be used for testing for timeout in the event something unexpected happens, like an I/O connection to VDD/VSS/MCLR/PGECx/PGEDx lost during command execution."
                                   + "\n@durationuncalmax Theoretical absolute max duration for execution on an uncalibrated device at highest temperature/slowest FRC in nanoseconds. If unknown, compute this value assuming a -40% slower than typical FRC PERIOD (not frequency). i.e: 1/(FRC frequency) * 0.60 * (number of FRC clocks required by the NVM state machine). This value may be used when programming blind-build/uncalibrated engineering samples during New Product Development cycles. It may also be used as the worst case timeout if the programming tool is explicitly supportive of engineering development or a user explicitly selects a \"safe mode\" to enable use of this timing parameter. NOTE: this should NEVER be used or supported on production programmers outside of a safe mode as deviation this far away from the expected timing case would be highly indicative of a defective device that shouldn't be in customer hands (i.e. high risk of incorrect application operation, which could pose a human safety hazard if allowed to still be programmed and used in an OEM's production line.";


        /**
         *
         * @author C12128
         */
        static class MemoryRange implements Comparable<MemoryRange>
        {
            static final String tag = "Range"; // Name of this XML tag
            String cname; // Required human name for the NVMOp
            long beginaddr = 0; // Required start address, inclusive
            long endaddr = 0; // Required end address, exclusive
            String supports; // Required, comma separated list of NVMOps
            boolean hasecc = false; // Optional
            boolean otp = false; // Optional
            @Override
            public String toString()
            {
                String options = String.format(" edc:hasecc=\"%s\" edc:otp=\"%s\"", Boolean.toString(hasecc), Boolean.toString(otp));
                return String.format("\n<edc:%s edc:cname=%-18s edc:beginaddr=\"0x%06X\" edc:endaddr=\"0x%06X\" edc:supports=\"%s\"%s/>", tag, "\"" + cname + "\"", beginaddr, endaddr, supports, options);
            }
            // Compares all class members for equality except the begin and end addresses
            @Override
            public int compareTo(MemoryRange x)
            {
                int ret;
                if(this.equals(x))
                    return 0;
                if(this.endaddr == x.endaddr)
                    return (int)(this.beginaddr - x.beginaddr);
                if(this.beginaddr < x.beginaddr)
                    return -1;
                if(this.beginaddr > x.beginaddr)
                    return 1;
                if(this.endaddr < x.endaddr)
                    return -1;
                if(this.endaddr > x.endaddr)
                    return 1;
                ret = this.cname.compareTo(x.cname);
                if(ret != 0)
                    return ret;
                ret = this.supports.compareTo(x.supports);
                if(ret != 0)
                    return ret;
                if((this.hasecc == x.hasecc) && (this.otp == x.otp))
                    return 0;
                if(this.hasecc && !x.hasecc)
                    return 1;
                else if(!this.hasecc && x.hasecc)
                    return -1;
                if(this.otp && !x.otp)
                    return 1;
                else if(!this.otp && x.otp)
                    return -1;
                return -2;
            }


            // Compares all class members for equality except the begin and end addresses
            static class MemoryRangeIgnoreAddrComparator implements Comparator<MemoryRange>
            {
                @Override
                public int compare(MemoryRange x, MemoryRange y)
                {
                    int ret;
                    if(x.equals(y))
                        return 0;
                    ret = x.cname.compareTo(y.cname);
                    if(ret != 0)
                        return ret;
                    ret = x.supports.compareTo(y.supports);
                    if(ret != 0)
                        return ret;
                    if((x.hasecc == y.hasecc) && (x.otp == y.otp))
                        return 0;
                    if(x.hasecc && !y.hasecc)
                        return 1;
                    else if(!x.hasecc && y.hasecc)
                        return -1;
                    if(x.otp && !y.otp)
                        return 1;
                    else if(!x.otp && y.otp)
                        return -1;

                    return -2;
                }
            }
        }


        static class SpecialRange extends MemoryRange
        {
            static final String tag = "Special";   // Name of this XML tag
            String somethingSpecial = "";

            public SpecialRange()
            {
            }

            public SpecialRange(MemoryRange mem)
            {
                this.beginaddr = mem.beginaddr;
                this.endaddr = mem.endaddr;
                this.cname = mem.cname;
                this.hasecc = mem.hasecc;
                this.otp = mem.otp;
                this.supports = mem.supports;
            }

            public SpecialRange(MemoryRange mem, String specialty)
            {
                somethingSpecial = specialty;
                this.beginaddr = mem.beginaddr;
                this.endaddr = mem.endaddr;
                this.cname = mem.cname;
                this.hasecc = mem.hasecc;
                this.otp = mem.otp;
                this.supports = mem.supports;
            }

            @Override
            public String toString()
            {
                String options = String.format(" edc:hasecc=\"%s\" edc:otp=\"%s\" %s", Boolean.toString(hasecc), Boolean.toString(otp), somethingSpecial);
                return String.format("\n<edc:%s edc:cname=%-18s edc:beginaddr=\"0x%06X\" edc:endaddr=\"0x%06X\" edc:supports=\"%s\"%s/>", tag, "\"" + cname + "\"", beginaddr, endaddr, supports, options);
            }
        };


        public static class ProgrammingMemories
        {
            static final String tag = "Memories";
            List<MemoryRange> ranges = new ArrayList<MemoryRange>();
            List<SpecialRange> specialRanges = new ArrayList<SpecialRange>();

            @Override
            public String toString()
            {
                List<String> ret = new ArrayList<>();

                if(ranges != null)
                {
                    for(MemoryRange r : ranges)
                    {
                        ret.add(r.toString());
                    }
                }
                if(specialRanges != null)
                {
                    for(SpecialRange r : specialRanges)
                    {
                        ret.add(r.toString());
                    }
                }
                return "\n<edc:" + tag + ">" + CatStringList(ret).replace("\n", "\n    ") + "\n</edc:" + tag + ">";
            }
        }


        public static class NVMOp implements Cloneable
        {
            String tag;
            String cname;
            String description;
            int nvmop;
            int nvmopmask;
            int destaddrinc = -1;
            int destbytes = -1;
            int srctype = -1;
            long srcaddr = -1;
            long durationtyp;
            long durationmax;
            long durationuncalmax;

            @Override
            public NVMOp clone() //throws CloneNotSupportedException
            {
                try
                {
                    return (NVMOp)(super.clone());
                }
                catch(CloneNotSupportedException ex)
                {   // Not possible, but Java wants it
                    return null;
                }
            }

            @Override
            public String toString()
            {
                String ret;

                ret = String.format("\n<edc:%s"
                                    + "\n    edc:cname=\"%s\""
                                    + "\n    edc:description=\"%s\""
                                    + "\n    edc:nvmop=\"0x%04X\""
                                    + "\n    edc:nvmopmask=\"0x%04X\"",
                                    tag, cname, description, nvmop, nvmopmask);
                if(srctype >= 0)
                    ret += String.format("\n    edc:srctype=\"%d\"", srctype);
                if(srcaddr >= 0)
                    ret += String.format("\n    edc:srcaddr=\"0x%X\"", srcaddr);
                if(destaddrinc >= 0)
                    ret += String.format("\n    edc:destaddrinc=\"0x%X\"", destaddrinc);
                if(destbytes >= 0)
                    ret += String.format("\n    edc:destbytes=\"%d\"", destbytes);
                ret += String.format("\n    edc:durationtyp=\"%d\""
                                     + "\n    edc:durationmax=\"%d\""
                                     + "\n    edc:durationuncalmax=\"%d\"/>",
                                     durationtyp, durationmax, durationuncalmax);
                return ret;
            }
        }

        @Override
        public String toString()
        {
            return toString(true);
        }

        public String toString(boolean includeHelpComment)
        {
            List<String> ret = new ArrayList<>();

            ret.add(String.format("\n<edc:" + tag + " "
                                  + "edc:partname=\"%s\" "
                                  + "edc:superset=\"%s\" "
                                  + "edc:devidaddr=\"0x%06X\" "
                                  + "edc:devidmask=\"0x%06X\" "
                                  + "edc:devid=\"0x%04X\" "
                                  + "edc:nvmconaddr=\"0x%04X\" "
                                  + "edc:nvmkeyaddr=\"0x%04X\" "
                                  + "edc:nvmkey1=\"0x%02X\" "
                                  + "edc:nvmkey2=\"0x%02X\" "
                                  + "edc:vddmin=\"%d\" "
                                  + "edc:vddmax=\"%d\" "
                                  + "edc:mclrlvl=\"%s\" "
                                  + "edc:description=\"%s\">\n",
                                  partname, superset, devidaddr, devidmask, devid, nvmconaddr, nvmkeyaddr, nvmkey1, nvmkey2, vddmin, vddmax, mclrlvl, description));
            if(includeHelpComment)
                ret.add("    <!--" + help.replace("\n", "\n        ") + "\n    -->");
            ret.add(memories.toString().replace("\n", "\n    ") + "\n");
            for(NVMOp op : nvmops.values())
            {
                ret.add(op.toString().replace("\n", "\n    "));
            }
            return CatStringList(ret) + "\n</edc:" + tag + ">";
        }

        // Copies all fields over except the tag and description and returns a reference to destOp
        public static NVMOp cloneOp(NVMOp destOp, NVMOp sourceOp)
        {
            destOp.cname = sourceOp.cname;
            destOp.destaddrinc = sourceOp.destaddrinc;
            destOp.destbytes = sourceOp.destbytes;
            destOp.durationmax = sourceOp.durationmax;
            destOp.durationtyp = sourceOp.durationtyp;
            destOp.durationuncalmax = sourceOp.durationuncalmax;
            destOp.nvmop = sourceOp.nvmop;
            destOp.nvmopmask = sourceOp.nvmopmask;
            destOp.srcaddr = sourceOp.srcaddr;
            destOp.srctype = sourceOp.srctype;
            return destOp;
        }

        @Override
        public int compareTo(Programming2 x)
        {
            return this.toString().compareTo(x.toString());
        }
    }
    static public void GenerateDifferentialSFRList(String inputDir, String outputFilename)
    {
//        List<File> picFiles = FindFilesRegEx(inputDir, ".*[\\.][Pp][Ii][Cc]", true);
//        if(picFiles.isEmpty())
//        {
//            System.err.printf("No .pic files found in: \"" + inputDir + "\"\n");
//            System.exit(-1);
//        }

        //Map<String, SFRAlternates> allSFRAddrMap = new HashMap<String, SFRAlternates>();    // SFR Name, List of addresses that the SFR appears at
//        List<BitFieldParams> BitFieldDifferences = new ArrayList<BitFieldParams>();
        PICDatabase picDB = new PICDatabase();
        Map<String, Long> devIDMap = new HashMap<>();               // "PartNumber", DevID
        TreeMap<Long, String> partNumIDMap = new TreeMap<>();       // DevID, "PartNumber"
        TreeMap<String, StringList> partsByDSNum = new TreeMap<>(); // dsNum, part_numbers
        DeviceProductLines allProducts = new DeviceProductLines();
        int architectureBits;   // 16 or 32

        SQLitePartDatabase sqlDB = new SQLitePartDatabase();
        sqlDB.openDatabase("ezbl_sqlite3_parts.db");
        TreeMap<String, EDCProperties> parts = sqlDB.getParts();

        Multifunction.WriteFile(outputFilename, null, false);   // Clear existing file contents

        for(String fullPartNumber : parts.keySet())
        {
            EDCProperties dbDev = parts.get(fullPartNumber);
            PICDevice dev = new PICDevice();
            dev.partNumber = fullPartNumber;
            System.out.println("Processing " + dev.partNumber);

            architectureBits = 16;
            if(dev.partNumber.startsWith("PIC32") || dev.partNumber.startsWith("M"))  // M == MEC, MGC, MTC
            {
                architectureBits = 32;
                dev.partClassName = "PIC32";
            }

            if(dev.partNumber.startsWith("dsPIC30"))
            {
                allProducts.dsPIC30F.add(dev.partNumber);
                dev.partClassName = "dsPIC30";
            }
            else if(dev.partNumber.startsWith("dsPIC33F"))
            {
                allProducts.dsPIC33F.add(dev.partNumber);
                dev.partClassName = "dsPIC33F";
            }
            else if(dev.partNumber.startsWith("dsPIC33E") || dev.partNumber.startsWith("dsPIC33D"))
            {
                allProducts.dsPIC33E.add(dev.partNumber);
                dev.partClassName = "dsPIC33E";
            }
            else if(dev.partNumber.startsWith("dsPIC33C"))
            {
                if(dev.partNumber.contains("RA"))
                {
                    continue;
                }
                dev.partClassName = "dsPIC33C";
                allProducts.dsPIC33C.add(dev.partNumber);
            }
            else if(dev.partNumber.startsWith("PIC24E"))
            {
                allProducts.PIC24E.add(dev.partNumber);
                dev.partClassName = "PIC24E";
            }
            else if(dev.partNumber.startsWith("PIC24H"))
            {
                allProducts.PIC24H.add(dev.partNumber);
                dev.partClassName = "PIC24H";
            }
            else if(dev.partNumber.startsWith("PIC24F"))
            {
                if(dev.partNumber.contains("K"))
                {
                    allProducts.PIC24FK.add(dev.partNumber);
                    dev.partClassName = "PIC24FK";
                }
                else
                {
                    allProducts.PIC24FJ.add(dev.partNumber);
                    dev.partClassName = "PIC24FJ";
                }
            }
            else if(dev.partNumber.startsWith("AC"))
                continue;

            picDB.partsByName.put(dev.partNumber, dev);  // Add to complete list of parsed parts

            dev.regsByName = sqlDB.getSFRs(dev.partNumber);
            dev.regsByAddr = sqlDB.getSFRsByAddr(dev.partNumber);
            for(EZBL_SFR sfr : dev.regsByAddr.values())
            {
                for(BitField bf : sfr.bitfields)
                {
                    dev.allBitFieldsInAllRegs.put(bf.name, bf);
                    SFRList regsWithBF = dev.regsWithBitfield.get(bf.name);
                    if(regsWithBF == null)
                    {
                        regsWithBF = new SFRList();
                    }
                    regsWithBF.list.add(sfr);
                    dev.regsWithBitfield.put(bf.name, regsWithBF);
                }
            }
            dev.datasheetID = String.valueOf(dbDev.dsNum);
            dev.dosID = String.valueOf(dbDev.dosNum);
            StringList partsInDS = partsByDSNum.get(dev.datasheetID);
            if(partsInDS == null)
            {
                partsInDS = new StringList();
            }
            partsInDS.list.add(dev.partNumber);
            partsByDSNum.put(dev.datasheetID, partsInDS);
            dev.devID = dbDev.devID;
            devIDMap.put(dev.partNumber, dev.devID);

            // Add device to PartNumIDMap, overwriting existing value if devID already used and this new part number has a shorter name
            if((partNumIDMap.get(dev.devID) == null) || (dev.partNumber.length() <= partNumIDMap.get(dev.devID).length()))
            {
                partNumIDMap.put(dev.devID, dev.partNumber);
            }

            // Decode all config bytes/config words
//            dev.addAll(IndexSFRDCRNodes(doc.getElementsByTagName("edc:DCRDef"), "DS.0"));
//                for(EZBL_SFR sfr : dev.regsByName.values())
//                {
//                    for(BitField b : sfr.bitfields)
//                    {
//                        String uniqueName;
//
//                        if(b.width == 16)
//                            uniqueName = sfr.name;
//                        else if(b.width == 1)
//                        {
//                            uniqueName = String.format("%1$s<%2$5s>", sfr.name, String.valueOf(b.position));
//                            uniqueName = String.format("%1$16s = %2$s", uniqueName, b.name);
//                        }
//                        else
//                        {
//                            uniqueName = String.format("%1$s<%2$2s:%3$2s>", sfr.name, String.valueOf(b.width - 1 + b.position), String.valueOf(b.position));
//                            uniqueName = String.format("%1$16s = %2$s", uniqueName, b.name);
//                        }
//                        picDB.bitFields.put(uniqueName, b);
//                    }
//                }
            if(dev.regsByName.containsKey("PSVPAG"))
            {
                dev.genericClassName = "generic-16bit";
                allProducts.generic_16bit.add(dev.partNumber);
            }
            else if(dev.partNumber.startsWith("PIC24F"))
            {
                dev.genericClassName = "generic-16bit-da";
                allProducts.generic_16bit_da.add(dev.partNumber);
            }
            else if(dev.partNumber.startsWith("dsPIC33C"))
            {
                dev.genericClassName = "generic-16dsp-ch";
                allProducts.generic_16dsp_ch.add(dev.partNumber);
            }
            else if(dev.partNumber.startsWith("dsPIC33E") || dev.partNumber.startsWith("PIC24E"))
            {
                dev.genericClassName = "generic-16bit-ep";
                allProducts.generic_16bit_ep.add(dev.partNumber);
            }
            else
            {
                dev.genericClassName = "generic-unknown";
                allProducts.generic_unknown.add(dev.partNumber);
            }

//                if(searchListSFRs != null)
//                {
//                    for(String search : searchListSFRs)
//                    {
//                        EZBL_SFR foundSFR = dev.regsByName.get(search);
//                        if(foundSFR == null)
//                        {
//                            continue;
//                        }
//                        BitFieldParams bfp = new BitFieldParams();
//                        bfp.isSFR = true;
//                        bfp.isDCR = foundSFR.isDCR;
//                        bfp.name = search;
//                        bfp.length = foundSFR.width;
//                        bfp.mask = foundSFR.bitsDefined;
//                        bfp.position = 0;
//                        bfp.parentName = foundSFR.srcModule;
//                        bfp.containedByGenericDevs.add(dev.genericClassName);
//                        bfp.addr = foundSFR.addr;
//                        bfp.devsWithMatchingAddr.add(dev.partNumber);
//                        bfp.devsWithMatchingParentAndPosition.add(dev.partNumber);
//                        bfp.devsWithMatchingAddrAndParentAndPosition.add(dev.partNumber);
//                        bfp.devsWithMatchingPosition.add(dev.partNumber);
//
//                        boolean foundMatch = false;
//                        for(BitFieldParams cmp : BitFieldDifferences)
//                        {
//                            if(bfp.compareTo(cmp) == 0)
//                            {
//                                foundMatch = true;
//                                cmp.devsWithMatchingAddr.add(dev.partNumber);
//                                cmp.devsWithMatchingParentAndPosition.add(dev.partNumber);
//                                cmp.devsWithMatchingAddrAndParentAndPosition.add(dev.partNumber);
//                                cmp.devsWithMatchingPosition.add(dev.partNumber);
//                                if(!cmp.containedByGenericDevs.contains(dev.genericClassName))
//                                {
//                                    cmp.containedByGenericDevs.add(dev.genericClassName);
//                                }
//                                break;
//                            }
//                        }
//                        if(!foundMatch)
//                        {
//                            BitFieldDifferences.add(bfp);
//                        }
//                    }
//                }
//
//                if(searchListBits != null)
//                {
//                    for(String search : searchListBits)
//                    {
//                        SFRList regsWithBitField = dev.regsWithBitfield.get(search);
//                        if(regsWithBitField == null)
//                        {
//                            continue;
//                        }
//                        for(EZBL_SFR foundBitSFRs : regsWithBitField.list)
//                        {
//                            BitField bf = foundBitSFRs.bitfieldByName.get(search);
//                            BitFieldParams bfp = new BitFieldParams();
//                            bfp.isBitfield = true;
//                            bfp.name = bf.name;
//                            bfp.length = bf.width;
//                            bfp.mask = bf.sfrBitmask;
//                            bfp.position = bf.position;
//                            bfp.parentName = bf.parentSFR.name;
//                            bfp.containedByGenericDevs.add(dev.genericClassName);
//                            bfp.addr = bf.parentSFR.addr;
//                            bfp.devsWithMatchingAddr.add(dev.partNumber);
//                            bfp.devsWithMatchingParentAndPosition.add(dev.partNumber);
//                            bfp.devsWithMatchingAddrAndParentAndPosition.add(dev.partNumber);
//                            bfp.devsWithMatchingPosition.add(dev.partNumber);
//
//                            boolean foundMatch = false;
//                            for(BitFieldParams cmp : BitFieldDifferences)
//                            {
//                                if(bfp.compareTo(cmp) == 0)
//                                {
//                                    foundMatch = true;
//                                    cmp.devsWithMatchingAddr.add(dev.partNumber);
//                                    cmp.devsWithMatchingParentAndPosition.add(dev.partNumber);
//                                    cmp.devsWithMatchingAddrAndParentAndPosition.add(dev.partNumber);
//                                    cmp.devsWithMatchingPosition.add(dev.partNumber);
//                                    if(!cmp.containedByGenericDevs.contains(dev.genericClassName))
//                                    {
//                                        cmp.containedByGenericDevs.add(dev.genericClassName);
//                                    }
//                                    break;
//                                }
//                            }
//                            if(!foundMatch)
//                            {
//                                BitFieldDifferences.add(bfp);
//                            }
//                        }
//                    }
//                }
//                for(EZBL_SFR s : dev.regsByAddr.values())    // List all addresses appearing in EDC for the given SFR name
//                {
//                    SFRAlternates altSFR = allSFRAddrMap.get(s.name);
//                    if(altSFR == null)
//                    {
//                        altSFR = new SFRAlternates();
//                        altSFR.name = s.name;
//                        altSFR.isDCR = s.isDCR;
//                        altSFR.lowestAddr = s.addr;
//                        altSFR.addrDevListPairs.put(s.addr, dev.partNumber);
//                        allSFRAddrMap.put(s.name, altSFR);
//                    }
//                    else
//                    {
//                        String applicableParts = altSFR.addrDevListPairs.get(s.addr);
//                        applicableParts += ", " + dev.partNumber;
//                        if(altSFR.lowestAddr > s.addr)
//                        {
//                            altSFR.lowestAddr = s.addr;
//                        }
//                        altSFR.addrDevListPairs.put(s.addr, applicableParts);
//                        allSFRAddrMap.put(s.name, altSFR);
//                    }
//                }
            // edc:Programming2 schema
            dev.prog = new Programming2();
            dev.prog.devid = dbDev.devID;
            dev.prog.devidmask = dbDev.devIDMask;
            dev.prog.devidaddr = dbDev.devIDAddr;
            dev.prog.mclrlvl = "vdd";
            dev.prog.nvmconaddr = dev.regsByName.get("NVMCON").addr;
            dev.prog.nvmkeyaddr = dev.regsByName.get("NVMKEY").addr;
            dev.prog.nvmkey1 = architectureBits == 16 ? 0x55L : 0xAA996655L;
            dev.prog.nvmkey2 = architectureBits == 16 ? 0xAAL : 0x556699AAL;
            dev.prog.partname = dev.partNumber;
            dev.prog.vddmax = 3600;
            dev.prog.vddmin = 3000;

            NVMOp op = new NVMOp();
            op.tag = "EraseChip";
            op.cname = "Chip Erase";
            op.description = "Intrinsically targetted erase command affecting all edc:Memories on the device necessary to be considered a blank device (excludes Executive Flash if possible). Ex: User Flash + Auxiliary Flash + FGS Config byte + FAS Config byte, or FBOOT + Active Partition Flash + Inactive Partition Flash.";
            op.nvmop = 0x400E;
            op.nvmopmask = 0x400F;
            op.durationtyp = 22900000L;
            op.durationmax = 24100000L;
            op.durationuncalmax = 32700000L;
            dev.prog.nvmops.put(op.tag, op);

            op = new NVMOp();
            op.tag = "EraseCourse";
            op.cname = "Page Erase";
            op.description = "Largest addressable sized flash erase command supported on the device, generally a Page Erase with all the same values as the edc:EraseFine tag, but alternatively used for faster erase times on Microchip internal flash cells that are row erasable. Durations are in integer nanoseconds.";
            op.nvmop = dbDev.coreType == CPUClass.mm ? 0x4004 : 0x4003;
            op.nvmopmask = 0x400F;
            op.destaddrinc = dbDev.eraseBlockSize;
            op.destbytes = architectureBits == 16 ? op.destaddrinc / 2 * 3 : op.destaddrinc;
            op.durationtyp = 22900000L;
            op.durationmax = 24100000L;
            op.durationuncalmax = 32700000L;
            dev.prog.nvmops.put(op.tag, op);

            op = dev.prog.nvmops.get("EraseCourse").clone();
            op.tag = "EraseFine";
            op.description = "Smallest sized flash erase command supported on the device, generally a Page Erase or Row Erase.  Durations are in integer nanoseconds.";
            dev.prog.nvmops.put(op.tag, op);

            op = new NVMOp();
            op.tag = "ProgramCourse";
            op.cname = "Row Program (64x)";
            op.description = "Largest sized flash programming command supported on the device for use when best programming speed is desired over addressing granularity. Normally a &quot;row&quot; program NVMCON command. If word/double word/quad word programming is the only supported programming method for the device, this edc:ProgramCourseBlock tag must duplicate the contents from the edc:ProgramFineBlock tag so software can choose one or the other on any device without needing an absolute definition of Fine vs Course.  Durations are in integer nanoseconds.";
            op.nvmop = 0x4002;
            op.nvmopmask = 0x400F;
            op.destaddrinc = 0x80;
            op.destbytes = architectureBits == 16 ? op.destaddrinc / 2 * 3 : op.destaddrinc;
            op.srctype = 1;
            op.srcaddr = 0xFA0000L;
            op.durationtyp = 1500000L;
            op.durationmax = 1790000L;
            op.durationuncalmax = 2150000L;
            if(dbDev.coreType == EZBLState.CPUClass.mm)
            {   // PIC32MM: all devices suppport bus mastered row programming
                op.nvmop = 0x4003;
                op.destaddrinc = 0x100;
                op.destbytes = op.destaddrinc;
                op.cname = "Row Program (64x bus mastered)";
                op.srctype = 3; // 3 = Packed/No phantom bytes; 2 = Unpacked RAM/has phantom bytes
                op.srcaddr = dev.regsByName.get("NVMSRCADDR").addr;
            }
            else if(dev.regsByName.containsKey("NVMSRCADR") || dev.regsByName.containsKey("NVMSRCADRL"))
            {   // Suppports bus mastered row programming
                op.cname = "Row Program (64x bus mastered)";
                op.srctype = 2; // 3 = Packed/No phantom bytes; 2 = Unpacked RAM/has phantom bytes
                if(dev.regsByName.containsKey("NVMSRCADR"))
                    op.srcaddr = dev.regsByName.get("NVMSRCADR").addr;
                else
                    op.srcaddr = dev.regsByName.get("NVMSRCADRL").addr;
            }
            dev.prog.nvmops.put(op.tag, op);

            op = new NVMOp();
            op.tag = "ProgramFine";
            op.cname = "Double-word Program";
            op.description = "Smallest sized flash programming command supported on the device for use when best addressing granularity is desired over programming speed. Normally a flash word, double word or quad word NVMCON programming command. Durations are in integer nanoseconds.";
            op.nvmop = 0x4001;
            op.nvmopmask = 0x400F;
            op.destaddrinc = 0x4;
            op.destbytes = architectureBits == 16 ? op.destaddrinc / 2 * 3 : op.destaddrinc;
            op.srctype = 1;
            op.srcaddr = 0xFA0000L;
            op.durationtyp = 48200L;
            op.durationmax = 57600L;
            op.durationuncalmax = 68800L;
            if(dbDev.coreType == EZBLState.CPUClass.mm)
            {
                op.nvmop = 0x4002;
                op.destaddrinc = 0x8;
                op.destbytes = architectureBits == 16 ? op.destaddrinc / 2 * 3 : op.destaddrinc;
                op.srctype = 1;
                op.srcaddr = dev.regsByName.get("NVMDATA0").addr;
            }
            dev.prog.nvmops.put(op.tag, op);

            if((dbDev.coreType != EZBLState.CPUClass.mm) && dev.regsByName.containsKey("FICD"))
            {
                if(dev.regsByName.get("FICD").addr >= 0x800000L)
                {   // Add WriteConfig command
                    op = new NVMOp();
                    op.tag = "WriteConfig";
                    op.cname = "Config Byte Write";
                    op.description = "Special command specific to writing one Configuration Byte on applicable devices. These Config Bytes behave as a self-erased EEPROM cell and do not require/support an equivalent erase command. Not all bits within the byte may be implemented and their default factory &quot;erased&quot; state generally isn't all '1's";
                    op.nvmop = 0x4000;
                    op.nvmopmask = 0x400F;
                    op.destaddrinc = 0x2;
                    op.destbytes = 1;
                    op.srctype = 1;
                    op.srcaddr = 0xFA0000L;
                    op.durationtyp = 24400000L;
                    op.durationmax = 25700000L;
                    op.durationuncalmax = 34800000L;
                    dev.prog.nvmops.put(op.tag, op);
                }
            }

            if(dev.partNumber.startsWith("dsPIC30"))
            {   // Update parameters for 5V and old style 24-bit Word Programming and Row Erase
                dev.prog.flashconfig = false;
                dev.prog.vddmax = 5500;
                dev.prog.vddmin = 4500;
                dev.prog.mclrlvl = "vpp";

                op = dev.prog.nvmops.get("EraseChip");
                op.nvmop = 0x406E;
                op.nvmopmask = 0x407F;
                op.description = "Erase Boot, Secure and General Segments, then erase FBS, FSS and FGS configuration registers. EEPROM and other Config bytes are retained.";
                op.durationtyp = (long)(0.004 * 1e9);                     // 4ms in data sheet for "ICSP Block Erase"
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                op = dev.prog.nvmops.get("EraseFine");
                op.cname = "Row Erase (32x)";
                op.nvmop = 0x4071;
                op.nvmopmask = 0x407F;
                op.destaddrinc = 0x40;
                op.destbytes = op.destaddrinc / 2 * 3;
                op.srcaddr = 0;
                op.srctype = 0;
                op.durationtyp = (long)(0.002 * 1e9);                     // 2ms in data sheet for erase/write
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                op = op.clone();
                op.tag = "EraseEECourse";
                op.cname = "EEPROM Row Erase (16x)";
                op.description = "Erase 16 EEPROM words (32 bytes)";
                op.nvmop = 0x4075;
                op.destaddrinc = 0x20;
                op.destbytes = op.destaddrinc;
                dev.prog.nvmops.put(op.tag, op);

                op = op.clone();
                op.tag = "EraseEEFine";
                op.cname = "EEPROM Word Erase (1x)";
                op.description = "Erase 1 EEPROM word (2 bytes)";
                op.nvmop = 0x4074;
                op.destaddrinc = 0x2;
                op.destbytes = op.destaddrinc;
                dev.prog.nvmops.put(op.tag, op);

                op = op.clone();
                op.tag = "ProgramEECourse";
                op.cname = "EEPROM Row Program (16x)";
                op.description = "Program 16 EEPROM words (32 bytes)";
                op.nvmop = 0x4005;
                op.destaddrinc = 0x20;
                op.destbytes = op.destaddrinc;
                dev.prog.nvmops.put(op.tag, op);

                op = op.clone();
                op.tag = "ProgramEEFine";
                op.cname = "EEPROM Word Program (1x)";
                op.description = "Program 1 EEPROM word (2 bytes)";
                op.nvmop = 0x4004;
                op.destaddrinc = 0x2;
                op.destbytes = op.destaddrinc;
                dev.prog.nvmops.put(op.tag, op);

                op = dev.prog.nvmops.get("ProgramFine");
                op.cname = "Row Program (32x)";
                op.description = "Program 32 instruction words at a row boundary (96 bytess)";
                op.nvmop = 0x4001;
                op.srcaddr = 0;
                op.srctype = 0;
                op.destaddrinc = 0x40;
                op.destbytes = op.destaddrinc / 2 * 3;

                cloneOp(dev.prog.nvmops.get("ProgramCourse"), op);

                op = dev.prog.nvmops.get("WriteConfig");
                op.nvmopmask = 0x4008;
                op.srcaddr = 0;
                op.srctype = 0;
                op.destaddrinc = 0x2;
                op.destbytes = 2;
                op.durationtyp = (long)(0.004 * 1e9);                     // No data in data sheet, assume 4ms like Block Erase
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

            }
            else if(dev.partNumber.startsWith("dsPIC33F") || dev.partNumber.startsWith("PIC24H"))
            {
                dev.prog.flashconfig = false;
                op = dev.prog.nvmops.get("EraseChip");
                op.cname = "Chip Erase";
                op.nvmop = 0x404F;
                op.nvmopmask = 0x404F;
                op.durationtyp = (long)((1.0 / 7370000) * 1.0 * (168517 + 355) * 1e9);      // 168517 Page Erase + 355 Word Write FRC cycles
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                op = dev.prog.nvmops.get("EraseFine");
                op.cname = "Page Erase";
                op.nvmop = 0x4042;
                op.nvmopmask = 0x404F;
                op.destaddrinc = 0x400;
                op.destbytes = op.destaddrinc / 2 * 3;
                op.srcaddr = 0;
                op.srctype = 0;
                op.durationtyp = (long)((1.0 / 7370000) * 1.0 * (168517) * 1e9);            // 168517 Page Erase FRC cycles
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                op = dev.prog.nvmops.get("ProgramFine");
                op.cname = "Word Program (1x)";
                op.nvmop = 0x4003;
                op.nvmopmask = 0x404F;
                op.destaddrinc = 0x2;
                op.destbytes = op.destaddrinc / 2 * 3;
                op.srcaddr = 0;
                op.srctype = 0;
                op.durationtyp = (long)((1.0 / 7370000) * 1.0 * (355) * 1e9);               // 355 Word Write FRC cycles
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                op = dev.prog.nvmops.get("ProgramCourse");
                op.cname = "Row Program (64x)";
                op.nvmop = 0x4001;
                op.destaddrinc = 0x80;
                op.destbytes = op.destaddrinc / 2 * 3;
                op.durationtyp = (long)((1.0 / 7370000) * 1.0 * (11064) * 1e9);             // 11064 Row Write FRC cycles
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                if((dev.regsByName.get("FICD") == null) || (dev.regsByName.get("FICD").addr < 0x800000L))
                {   // Flash config words (dsPIC33FJ16GP102, dsPIC33FJ32GP104, dsPIC33FJ06GS001, dsPIC33FJ06GS202A, dsPIC33FJ09GS302, dsPIC33FJ32MC104)
                    cloneOp(dev.prog.nvmops.get("ProgramCourse"), dev.prog.nvmops.get("ProgramFine"));  // No row programming on these tiny parts
                }
                else
                {   // Most devices in this class - real fuses
                    op = dev.prog.nvmops.get("WriteConfig");
                    op.nvmopmask = 0x404F;
                    op.durationtyp = (long)((1.0 / 7370000) * 1.0 * (168517 + 11064) * 1e9);    // 168517 Page Erase + 11064 Row Write FRC cycles
                    op.durationmax = (long)(op.durationtyp * 1.07);
                    op.durationuncalmax = (long)(op.durationtyp * 1.40);
                    op.srctype = 0;
                    op.srcaddr = 0;
                }
            }
            else if(dev.partNumber.startsWith("PIC24FJ") && dev.regsByName.get("NVMCON").bitfieldByName.containsKey("ERASE"))
            {   // Older PIC24FJ devices with 24-bit word programming
                dev.prog.vddmin = 2750;

                op = dev.prog.nvmops.get("EraseChip");
                op.cname = "Chip Erase";
                op.nvmop = 0x404F;
                op.nvmopmask = 0x404F;
                op.durationtyp = (long)(0.00301 * 1e6);                  // ???
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                op = dev.prog.nvmops.get("EraseFine");
                op.cname = "Page Erase";
                op.nvmop = 0x4042;
                op.nvmopmask = 0x404F;
                op.destaddrinc = 0x400;
                op.destbytes = op.destaddrinc / 2 * 3;
                op.srcaddr = 0;
                op.srctype = 0;
                op.durationtyp = (long)(0.003 * 1e6);                   // 3ms in PIC24FJ128GA010 data sheet
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                cloneOp(dev.prog.nvmops.get("EraseCourse"), op);

                op = dev.prog.nvmops.get("ProgramFine");
                op.cname = "Word Program (1x)";
                op.nvmop = 0x4003;
                op.nvmopmask = 0x404F;
                op.destaddrinc = 0x2;
                op.destbytes = op.destaddrinc / 2 * 3;
                op.srcaddr = 0;
                op.srctype = 0;
                op.durationtyp = (long)((1.0 / 8000000) * 1.0 * (355) * 1e9);               // 355 Word Write FRC cycles
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                cloneOp(dev.prog.nvmops.get("ProgramCourse"), op);

                op = dev.prog.nvmops.get("ProgramCourse");
                op.cname = "Row Program (64x)";
                op.nvmop = 0x4001;
                op.destaddrinc = 0x80;
                op.destbytes = op.destaddrinc / 2 * 3;
                op.durationtyp = (long)((1.0 / 8000000) * 1.0 * (11064) * 1e9);             // 11064 Row Write FRC cycles
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);
            }
            else if(dev.partNumber.startsWith("PIC24F") && !dev.partNumber.startsWith("PIC24FJ"))   // PIC24FxxK device
            {
                dev.prog.flashconfig = false;
                dev.prog.vddmax = 5000;
                dev.prog.vddmin = dev.partNumber.startsWith("PIC24FV") ? 2700 : 2000;

                op = dev.prog.nvmops.get("EraseChip");
                op.nvmop = 0x4064;
                op.nvmopmask = 0x507F;
                op.description = "Erase Flash and Configuration registers (Programming Executive flash retained)";
                op.durationtyp = (long)(0.004 * 1e9);                     // ???
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                op = dev.prog.nvmops.get("EraseFine");
                op.cname = "Row Erase (32x)";
                op.nvmop = 0x4058;
                op.nvmopmask = 0x507F;
                op.destaddrinc = 0x40;
                op.destbytes = op.destaddrinc / 2 * 3;
                op.srcaddr = 0;
                op.srctype = 0;
                op.durationtyp = (long)(0.002 * 1e9);                     // ???
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                op = cloneOp(dev.prog.nvmops.get("EraseCourse"), op);
                op.cname = "Row Erase (128x)";
                op.nvmop = 0x405A;
                op.nvmopmask = 0x507F;
                op.srcaddr = 0;
                op.srctype = 0;
                op.destaddrinc *= 4;
                op.destbytes *= 4;
                op.durationtyp *= 4;
                op.durationmax *= 4;
                op.durationuncalmax *= 4;

                op = dev.prog.nvmops.get("ProgramFine");
                op.cname = "Row Program (32x)";
                op.nvmop = 0x4004;
                op.nvmopmask = 0x507F;
                op.destaddrinc = 0x40;
                op.destbytes = op.destaddrinc / 2 * 3;
                op.srcaddr = 0;
                op.srctype = 0;
                op.durationtyp = (long)(0.002 * 1e9);                     // ???
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);

                cloneOp(dev.prog.nvmops.get("ProgramCourse"), op);

                op = dev.prog.nvmops.get("EraseCourse").clone();
                op.tag = "EraseEECourse";
                op.cname = "EEPROM Row Erase (8x)";
                op.description = "Erase 8 EEPROM words (16 bytes)";
                op.nvmop = 0x405A;
                op.srcaddr = 0;
                op.srctype = 0;
                op.destaddrinc = 0x10;
                op.destbytes = op.destaddrinc;
                dev.prog.nvmops.put(op.tag, op);

                op = op.clone();
                op.tag = "EraseEEFine";
                op.cname = "EEPROM Word Erase (1x)";
                op.description = "Erase 1 EEPROM word (2 bytes)";
                op.nvmop = 0x4058;
                op.srcaddr = 0;
                op.srctype = 0;
                op.destaddrinc = 0x2;
                op.destbytes = op.destaddrinc;
                dev.prog.nvmops.put(op.tag, op);

                op = op.clone();
                op.tag = "ProgramEEFine";
                op.cname = "EEPROM Word Program (1x)";
                op.description = "Program 1 EEPROM word (2 bytes)";
                op.nvmop = 0x4004;
                op.srcaddr = 0;
                op.srctype = 0;
                op.destaddrinc = 0x2;
                op.destbytes = op.destaddrinc;
                dev.prog.nvmops.put(op.tag, op);

                op = dev.prog.nvmops.get("WriteConfig");
                op.nvmop = 0x4004;
                op.nvmopmask = 0x507F;
                op.srcaddr = 0;
                op.srctype = 0;
                op.destbytes = 0x2;
                op.destaddrinc = 0x2;
                op.durationtyp = (long)(0.004 * 1e9);                     // No data in data sheet, assume 4ms like Block Erase
                op.durationmax = (long)(op.durationtyp * 1.07);
                op.durationuncalmax = (long)(op.durationtyp * 1.40);
            }
            else if(dbDev.coreType == EZBLState.CPUClass.e)
            {
                dev.prog.vddmin = 3200;
                if(dev.partNumber.contains("33EV"))
                {
                    dev.prog.vddmin = 4500;
                    dev.prog.vddmax = 5500;
                }
            }

            if(dev.regsByName.containsKey("FBOOT"))
            {
                // Add ProgramFBOOT command
                op = dev.prog.nvmops.get("ProgramFine").clone();
                op.tag = "ProgramFBOOT";
                op.cname = "Program FBOOT Double-word";
                op.description = "Special command required to program FBOOT global Flash Configuration Word. Target address is intrinsic.";
                op.nvmop = 0x4008;
                dev.prog.nvmops.put(op.tag, op);

                // Add EraseInactive command
                op = dev.prog.nvmops.get("EraseFine").clone();
                op.tag = "EraseInactive";
                op.cname = "Erase Inactive Partition";
                op.description = "Special command to erase all of the currently Inactive Parition pages. Target address is intrinsic at 0x4000000+. The absolute identity of the target partition can be evaluated by checking NVMCON<P2ACTIV>.";
                op.nvmop = 0x4004;
                dev.prog.nvmops.put(op.tag, op);
            }

            List<MemoryRegion> dbProgSpaces = sqlDB.getMemories(SQLitePartDatabase.MemoryTable.BootloadableRanges, dev.partNumber, null, null);
            for(MemoryRegion mr : dbProgSpaces)
            {
                MemoryRange e = new MemoryRange();
                e.cname = mr.name;

                if((dbDev.coreType == CPUClass.mm) || (dbDev.coreType == CPUClass.c) || (dbDev.coreType == CPUClass.b) || dev.partNumber.startsWith("dsPIC33EV")
                   || dev.regsByName.containsKey("ECCCON") || dev.regsByName.containsKey("ECCCONL") || dev.regsByName.containsKey("ECCCON1") || dev.regsByName.containsKey("ECCCON1L")
                   || dev.allBitFieldsInAllRegs.containsKey("ECCDBE") || dev.allBitFieldsInAllRegs.containsKey("ECCIF"))
                    e.hasecc = true;

                if(mr.name.equalsIgnoreCase("bootcfg"))
                {
                    e.supports = "EraseChip,ProgramFBOOT";
                    e.cname = "FBOOT Config Flash";
                    mr.alignToProgSize();
                }
                else if(mr.type == MemoryRegion.MemType.OTP)
                {
                    e.cname = "OTP";
                    e.otp = true;
                    e.supports = "ProgramCourse,ProgramFine";
                    mr.alignToProgSize();
                }
                else if(((mr.type == MemoryRegion.MemType.ROM) || (mr.type == MemoryRegion.MemType.FLASHFUSE)) && (mr.partition == MemoryRegion.Partition.single))    // Ordinary flash
                {
                    e.cname = "Primary Flash";
                    e.supports = "EraseChip,EraseCourse,EraseFine,ProgramCourse,ProgramFine";
                    mr.alignToEraseSize();
                }
                else if(((mr.type == MemoryRegion.MemType.ROM) || (mr.type == MemoryRegion.MemType.FLASHFUSE)) && (mr.partition == MemoryRegion.Partition.partition1))
                {
                    e.cname = "Active Partition Flash";
                    e.supports = "EraseChip,EraseInactive,EraseCourse,EraseFine,ProgramCourse,ProgramFine";
                    mr.alignToEraseSize();
                }
                else if(((mr.type == MemoryRegion.MemType.ROM) || (mr.type == MemoryRegion.MemType.FLASHFUSE)) && (mr.partition == MemoryRegion.Partition.partition2))
                {
                    e.cname = "Inactive Partition Flash";
                    e.supports = "EraseChip,EraseInactive,EraseCourse,EraseFine,ProgramCourse,ProgramFine";
                    mr.alignToEraseSize();
                }
                else if(mr.type == MemoryRegion.MemType.EEPROM)
                {
                    e.cname = "EEPROM";
                    e.supports = "EraseEECourse,EraseEEFine,ProgramEEFine";
                    mr.alignToProgSize();
                }
                else if(mr.name.equals("auxflash"))
                {
                    e.cname = "Auxiliary Flash";
                    e.supports = "EraseChip,EraseAux,EraseFine,EraseCourse,ProgramCourse,ProgramFine";
                    mr.alignToEraseSize();
                }
                else if(mr.type == MemoryRegion.MemType.TEST)
                {
                    if(dev.partNumber.startsWith("PIC24F04"))   // Exclude Executive Flash on PIC24F04KA201 (+PIC24F04KA200). There should be no such data in DDT.
                        continue;
                    e.cname = "Executive Flash";
                    e.supports = "EraseCourse,EraseFine,ProgramCourse,ProgramFine";
                    mr.alignToProgSize();
                }
                else if(mr.type == MemoryRegion.MemType.BYTEFUSE)
                {
                    e.cname = "Config Bytes";
                    e.supports = "WriteConfig";
                    mr.alignToProgSize();
                }
                else if(mr.type == MemoryRegion.MemType.FLASHFUSE)
                {
                    e.supports = "EraseChip,EraseCourse,EraseFine,ProgramCourse,ProgramFine";
                }
                e.beginaddr = mr.startAddr;
                e.endaddr = mr.endAddr;

                dev.prog.memories.ranges.add(e);
            }

//                DecodedNodeList progSpaces = IndexNodes(doc.getElementsByTagName("edc:ProgramSpace"), 1);
//                for(DecodedNode n : progSpaces.list)
//                {
//                    MemoryRange e = new MemoryRange();
//                    
//                    e.cname = n.attribs.get("edc:regionid");
//                    if(e.cname == null)
//                        e.cname = n.tagName.substring(4);
//                    e.beginaddr = n.getAttrAsLong("edc:beginaddr", -1);
//                    e.endaddr = n.getAttrAsLong("edc:endaddr", -1);
//                    if(e.cname.equalsIgnoreCase("uniqueid") || e.cname.equalsIgnoreCase("calibration") || e.cname.equalsIgnoreCase("devid")) // Ignore read-only locations (cannot be erased/programmed in TMOD0/TMOD1)
//                        continue;
//                    if((e.beginaddr == -1L) && (e.endaddr == -1L))    // Ignore items without an assigned location (i.e. "aivt" on newer devices like PIC24FJ256GA705)
//                        continue;
//                    if(dev.partNumber.startsWith("PIC32MM") || dev.partNumber.startsWith("dsPIC33EV") || dev.partNumber.startsWith("dsPIC33C")
//                       || dev.regsByName.containsKey("ECCCON") || dev.regsByName.containsKey("ECCCONL") || dev.regsByName.containsKey("ECCCON1") || dev.regsByName.containsKey("ECCCON1L")
//                       || dev.allBitFieldsInAllRegs.containsKey("ECCDBE") || dev.allBitFieldsInAllRegs.containsKey("ECCIF"))
//                        e.hasecc = true;
//                    
//                    if(e.cname.equalsIgnoreCase("FBOOT") || e.cname.equalsIgnoreCase("bootcfg"))
//                    {
//                        e.supports = "EraseChip,ProgramFBOOT";
//                        e.cname = "FBOOT Config Flash";
//                    }
//                    else if(e.cname.toLowerCase().endsWith("otp") || (dev.partNumber.startsWith("dsPIC33EV") && e.cname.toLowerCase().contains("userid")))
//                    {
//                        e.cname = "OTP";
//                        e.otp = true;
//                        e.supports = "ProgramCourse,ProgramFine";
//                    }
//                    else if(e.beginaddr < 0x400000L)    // Ordinary flash
//                    {
//                        e.cname = "Primary Flash";
//                        e.supports = "EraseChip,EraseCourse,EraseFine,ProgramCourse,ProgramFine";
//                    }
//                    else if(e.beginaddr < 0x7F0000L)
//                    {
//                        e.cname = "Inactive Partition Flash";
//                        e.supports = "EraseChip,EraseInactive,EraseCourse,EraseFine,ProgramCourse,ProgramFine";
//                    }
//                    else if(e.cname.matches("eedata") || (e.beginaddr == 0x7FFC00L))  // 0x7FFC00 start address
//                    {
//                        e.cname = "EEPROM";
//                        e.supports = "EraseEECourse,EraseEEFine,ProgramEEFine";
//
////                        op = new NVMOp();
////                        op.tag = "EraseEE";
////                        op.cname = "Erase All EEPROM";
////                        op.description = "Erases all EEPROM words";
////                        op.destaddrinc = 0x400;
////                        op.destbytes = op.destaddrinc;
////                        op.nvmop = 0x4050;
////                        op.nvmopmask = 0x504F;
////                        op.srcaddr = 0;
////                        op.srctype = 0;
////                        op.durationtyp = (long)(0.004 * 1e9);  // TODO
////                        op.durationmax = (long)(op.durationtyp * 1.07);
////                        op.durationuncalmax = (long)(op.durationtyp * 1.40);
////                        dev.prog.nvmops.put("EraseEE", op);
////
////                        op = op.clone();
////                        op.tag = "EraseEECourse";
////                        op.cname = "Erase EEPROM (8x Words)";
////                        op.description = "Erases large sized block of EEPROM (8x Words or 16 bytes each)";
////                        op.destaddrinc = 0x10;
////                        op.destbytes = op.destaddrinc;
////                        dev.prog.nvmops.put("EraseEECourse", op);
////
////                        op = op.clone();
////                        op.tag = "EraseEEFine";
////                        op.cname = "Erase EEPROM (1x Word)";
////                        op.description = "Erases minimally sized block of EEPROM (1 Word or 2 data bytes)";
////                        op.destaddrinc = 0x2;
////                        op.destbytes = op.destaddrinc;
////                        dev.prog.nvmops.put("EraseEEFine", op);
////
////                        op = op.clone();
////                        op.tag = "ProgramEEFine";
////                        op.cname = "Program EEPROM (1x Word)";
////                        op.description = "Programs minimally sized block of EEPROM (1 Word or 2 data bytes)";
////                        op.destaddrinc = 0x2;
////                        op.destbytes = op.destaddrinc;
////                        dev.prog.nvmops.put("ProgramEEFine", op);
//                    }
//                    else if(e.beginaddr < 0x800000L)
//                    {
//                        e.cname = "Auxiliary Flash";
//                        e.supports = "EraseChip,EraseAux,EraseFine,EraseCourse,ProgramCourse,ProgramFine";
//                    }
//                    else if(e.beginaddr < 0x900000L)
//                    {
//                        if(dev.partNumber.startsWith("PIC24F04"))   // Exclude Executive Flash on PIC24F04KA201 (+PIC24F04KA200). There should be no such data in DDT.
//                            continue;
//                        e.cname = "Executive Flash";
//                        e.supports = "EraseCourse,EraseFine,ProgramCourse,ProgramFine";
//                    }
//                    else if((e.beginaddr & 0xFF0000L) == 0xF80000L)
//                    {
//                        if(e.cname.contains("cfgworm") || n.tagName.equalsIgnoreCase("edc:ConfigWORMSector")) // Ignore Config SFRs where Flash Config Words are used
//                            continue;
//                        e.cname = "Config Bytes";
//                        e.supports = "WriteConfig";
//                    }
//                    else
//                    {
//                        e.supports = "EraseChip,EraseCourse,EraseFine,ProgramCourse,ProgramFine";
//                    }
//                    
//                    if((e.beginaddr < 0x900000L) && !e.cname.equals("EEPROM") && !e.cname.equals("OTP") && !e.cname.equals("FBOOT Special Config"))//if(dev.prog.nvmops.get("EraseCourse").destaddrinc >= 0x400)
//                    {
//                        e.beginaddr -= e.beginaddr % dev.prog.nvmops.get("EraseCourse").destaddrinc;
//                        if(n.attribs.containsKey("edc:appbeginaddr") && n.attribs.containsKey("edc:appendaddr"))
//                        {
//                            e.endaddr = Long.decode(n.attribs.get("edc:appendaddr"));
//                            e.endaddr -= Long.decode(n.attribs.get("edc:appbeginaddr")) - e.beginaddr;
//                        }
//                        if(e.endaddr % dev.prog.nvmops.get("EraseCourse").destaddrinc != 0)
//                            e.endaddr += dev.prog.nvmops.get("EraseCourse").destaddrinc - (e.endaddr % dev.prog.nvmops.get("EraseCourse").destaddrinc);
//                    }
//                    
//                    String partitionMode = n.attribs.get("edc:partitionmode");
//                    if((partitionMode == null) || (partitionMode.equalsIgnoreCase("single")))
//                    {
//                        dev.prog.memories.ranges.add(e);
//                    }
//                    else
//                    {
//                        if(e.cname.equals("Primary Flash"))
//                            e.cname = "Active Partition Flash";
//                        SpecialRange e2 = new SpecialRange(e);
//                        e2.somethingSpecial = "edc:specialty=\"Dual Partition\"";
//                        dev.prog.memories.specialRanges.add(e2);
//                    }
//            }
            Collections.sort(dev.prog.memories.ranges);
            for(int i = 0; i < dev.prog.memories.ranges.size(); i++)
            {
                MemoryRange mem = dev.prog.memories.ranges.get(i);
                for(int j = i + 1; j < dev.prog.memories.ranges.size(); j++)
                {
                    MemoryRange mem2 = dev.prog.memories.ranges.get(j);
                    if(new MemoryRangeIgnoreAddrComparator().compare(mem, mem2) != 0)
                        break;
                    if((mem.beginaddr <= mem2.endaddr) && (mem.endaddr >= mem2.beginaddr))
                    {
                        mem.beginaddr = (mem.beginaddr > mem2.beginaddr) ? mem2.beginaddr : mem.beginaddr;
                        mem.endaddr = (mem.endaddr < mem2.endaddr) ? mem2.endaddr : mem.endaddr;
                        dev.prog.memories.ranges.remove(j--);
                    }
                }
            }

            // Add family superset names to the edc:Programming2 data (our dev structures)
            String outData = "\n\n\n<!-- http://www.microchip.com/" + dev.partNumber + " DS number: " + dev.datasheetID + " -->" + dev.prog.toString(dev.partNumber.equals(parts.firstKey()));
            Multifunction.WriteFile(outputFilename, outData.getBytes(), true);
            dev.regsByAddr = null;
            dev.regsByName = null;
            dev.regsWithBitfield = null;
        }

        // All .pic files processed. Find superset part name in each family and populate the top level for it.
        System.out.printf("Devices Families:\n");
        for(StringList dsPartList : partsByDSNum.values())
        {
            dsPartList.sortPartNumbers();
            dsPartList.topString = dsPartList.list.get(0) + " family";
            System.out.printf("  %s\n", dsPartList.list.get(0));
        }

//        // Add family superset names to the edc:Programming2 data (our dev structures)
//        int i = 0;
//        for(PICDevice dev : picDB.partsByName.values())
//        {
//            dev.supersetPartNumber = partsByDSNum.get(dev.datasheetID).list.get(0);
//            dev.prog.superset = dev.supersetPartNumber;
//
//            progXML.add("\n<!-- http://www.microchip.com/" + dev.partNumber + " DS number: " + dev.datasheetID + " -->" + dev.prog.toString(i++ == 0));
//        }
//        Collections.sort(BitFieldDifferences);
//
//        String[] partList = new String[devIDMap.size()];
//        devIDMap.keySet().toArray(partList);
//        Arrays.sort(partList, new PICNameComparator());
//
//        Map<String, StringList> fieldsOnGeneric = new TreeMap<String, StringList>();
//        List<String> outLines = new ArrayList<String>(partList.length * 5);
//
//        outLines.add(String.format("Searched %1$d part numbers\n\n", devIDMap.size()));
//
//        for(BitFieldParams bfp : BitFieldDifferences)
//        {
//            bfp.GroupMatchingDevNames(allProducts, partsByDSNum);
//            String foundItem;
//
//            if(bfp.isSFR)   // SFR searchTags
//            {
//                String formatStr = bfp.isDCR
//                                   ? "0x%1$06X %2$-10s<%3$s:%4$s>    // %5$s\n"
//                                   : "  0x%1$04X %2$-10s<%3$s:%4$s>    // %5$s\n";
//                foundItem = String.format(formatStr, bfp.addr, bfp.name, bfp.position + bfp.length - 1, bfp.position, CatStringList(bfp.devsWithMatchingAddr, ", "));
//            }
//            else if(bfp.isBitfield) // Bitfield searchTags
//            {
//                foundItem = String.format("%1$s<%2$s>, #%3$d", bfp.parentName, bfp.name, bfp.position);
//                String extras = "";
//                String onParts = CatStringList(bfp.devsWithMatchingParentAndPosition, ", ");
//
//                if(bfp.parentName.matches("^IEC.*|^IFS.*|^IPC.*"))
//                {
//                    int regNum = Integer.decode(bfp.parentName.substring(3));
//                    extras = String.format("IRQ %1$3d", regNum * 16 + bfp.position);
//                }
//                else if(bfp.parentName.matches("^PMD.*"))
//                {
//                    int regNum = Integer.decode(bfp.parentName.substring(3));
//                    extras = String.format("PMD bit offset %1$3d", (regNum - 1) * 16 + bfp.position);
//                }
//
//                foundItem = String.format("%1$-21s // %2$s On: %3$s\n", foundItem, extras, onParts);
//            }
//            else
//            {
//                continue;
//            }
//
//            for(String genericDev : bfp.containedByGenericDevs)
//            {
//                StringList thingList = fieldsOnGeneric.get(genericDev);
//                if(thingList == null)
//                {
//                    thingList = new StringList();
//                }
//                thingList.list.add(foundItem);
//                fieldsOnGeneric.put(genericDev, thingList);
//            }
//
//            outLines.add(foundItem);
//        }
//
//        outLines.add("\n\n");
//        FieldsOnGenericDevice genericDevs = new FieldsOnGenericDevice();
//        for(BitFieldParams dif : BitFieldDifferences)
//        {
//            if(dif.containedByGenericDevs.contains("generic-16bit"))
//            {
//                genericDevs.generic_16bit.add(dif);
//            }
//            if(dif.containedByGenericDevs.contains("generic-16bit-da"))
//            {
//                genericDevs.generic_16bit_da.add(dif);
//            }
//            if(dif.containedByGenericDevs.contains("generic-16bit-ep"))
//            {
//                genericDevs.generic_16bit_ep.add(dif);
//            }
//            if(dif.containedByGenericDevs.contains("generic-16dsp-ch"))
//            {
//                genericDevs.generic_16dsp_ch.add(dif);
//            }
//        }
//        genericDevs.sortByFieldName();
//        outLines.add("\n\ngeneric-16bit has:");
//        for(BitFieldParams bfp : genericDevs.generic_16bit)
//        {
//            if(bfp.isBitfield)
//            {
//                outLines.add(String.format("\n    %1$-8s = %2$s<%3$d>\t// %4$s", bfp.name, bfp.parentName, bfp.position, CatStringList(bfp.devsWithMatchingParentAndPosition, ", ")));
//            }
//            else    // SFR or DCR
//            {
//                outLines.add(String.format("\n    %1$-8s 0x%2$04X // %3$s", bfp.name, bfp.addr, CatStringList(bfp.devsWithMatchingAddr, ", ")));
//            }
//        }
//        outLines.add("\n\ngeneric-16bit-da has:");
//        for(BitFieldParams bfp : genericDevs.generic_16bit_da)
//        {
//            if(bfp.isBitfield)
//            {
//                outLines.add(String.format("\n    %1$-8s = %2$s<%3$d>\t// %4$s", bfp.name, bfp.parentName, bfp.position, CatStringList(bfp.devsWithMatchingParentAndPosition, ", ")));
//            }
//            else    // SFR or DCR
//            {
//                outLines.add(String.format("\n    %1$-8s 0x%2$04X // %3$s", bfp.name, bfp.addr, CatStringList(bfp.devsWithMatchingAddr, ", ")));
//            }
//        }
//        outLines.add("\n\ngeneric-16bit-ep has:");
//        for(BitFieldParams bfp : genericDevs.generic_16bit_ep)
//        {
//            if(bfp.isBitfield)
//            {
//                outLines.add(String.format("\n    %1$-8s = %2$s<%3$d>\t// %4$s", bfp.name, bfp.parentName, bfp.position, CatStringList(bfp.devsWithMatchingParentAndPosition, ", ")));
//            }
//            else    // SFR or DCR
//            {
//                outLines.add(String.format("\n    %1$-8s 0x%2$04X // %3$s", bfp.name, bfp.addr, CatStringList(bfp.devsWithMatchingAddr, ", ")));
//            }
//        }
//        outLines.add("\n\ngeneric-16dsp-ch has:");
//        for(BitFieldParams bfp : genericDevs.generic_16dsp_ch)
//        {
//            if(bfp.isBitfield)
//            {
//                outLines.add(String.format("\n    %1$-8s = %2$s<%3$d>\t// %4$s", bfp.name, bfp.parentName, bfp.position, CatStringList(bfp.devsWithMatchingParentAndPosition, ", ")));
//            }
//            else    // SFR or DCR
//            {
//                outLines.add(String.format("\n    %1$-8s 0x%2$04X // %3$s", bfp.name, bfp.addr, CatStringList(bfp.devsWithMatchingAddr, ", ")));
//            }
//        }
//
////        for(String genericDev : fieldsOnGeneric.keySet())
////        {
////            StringList fieldReport = fieldsOnGeneric.get(genericDev);
////            fieldReport.sortSFRNames();
////            outLines.add("\n" + genericDev + " has:\n    " + CatStringList(fieldReport.list, "    "));
////        }
//        outLines.add("\n\n#if defined(DEVID) || defined(SUPPRESS_DEVID_DEF)");
//        for(String dev : partList)
//        {
//            outLines.add("\n#elif defined(__" + dev + "__)");
//            outLines.add(String.format("\n#define DEVID 0x%1$04X", devIDMap.get(dev)));
//        }
//        outLines.add("\n#else");
//        outLines.add("\n#define DEVID 0x0000");
//        outLines.add("\n#endif");
//
//        outLines.add("\n\n#define LOOKUP_DEVID(dev_name) (                                                \\");
//        for(String dev : partList)
//        {
//            outLines.add(String.format("\n            %1$-50s) == 0 ? 0x%2$04X : \\", "__builtin_strcmp(dev_name, \"" + dev + "\"", devIDMap.get(dev)));
//        }
//        outLines.add(String.format("\n            0x0000)    /* Device not found - may need to regenerate lookup table */"));
//
//        outLines.add("\n\n#define LOOKUP_DEVNAME(devid) (                          \\");
//        for(Long devid : partNumIDMap.keySet())
//        {
//            outLines.add(String.format("\n            (devid) == 0x%1$04X ? %2$-22s : \\", devid, "\"" + partNumIDMap.get(devid) + "\""));
//        }
//        outLines.add(String.format("\n            \"Unknown\")    /* Device not found - may need to regenerate lookup table */"));
//        List<SFRAlternates> alts = new ArrayList<SFRAlternates>();
//        alts.addAll(allSFRAddrMap.values());
//        Collections.sort(alts);
//        String formatStr;
//        for(SFRAlternates a : alts)
//        {
//            outLines.add(String.format("\n%1$-16s ", a.name));
//            for(long addr : a.addrDevListPairs.keySet())
//            {
//                formatStr = a.isDCR ? "    0x%1$06X (%2$3d)" : "    0x%1$04X (%2$3d)";
//                outLines.add(String.format(formatStr, addr, a.addrDevListPairs.get(addr).split(", ").length));
//            }
//        }
//
//        // Unique bitfield names across all parts
//        List<String> uniqueBitfields = new ArrayList<String>(picDB.bitFields.size() + 1);
//        uniqueBitfields.add("\n\n\nAll unique SFR<bitfield:bitlen> bitfields across all parts:");
//        for(String bitfield : picDB.bitFields.keySet())
//        {
//            uniqueBitfields.add(bitfield);
//        }
//        outLines.add(CatStringList(uniqueBitfields, "\n"));
//        outLines.add(CatStringList(progXML, "\n\n"));
//
//        String fileOutContents = Multifunction.CatStringList(outLines);
//        outLines.clear();
//        Multifunction.UpdateFileIfDataDifferent(outputFilename, fileOutContents, true);
    }

    static public int GeneratePICAbstractionHeader(String partNumber, String outputFilename)
    {
        String fileOutContents;
        List<String> hStrings = new ArrayList<String>();
        List<String> asmStrings = new ArrayList<String>();
        List<PairWithText> RPOutEncodings = new ArrayList<PairWithText>();
        List<PairWithText> pinNumberFunctionMappings = new ArrayList<PairWithText>();
        int ret = 0;
        Document doc = null;

        doc = PICXMLLoader.LoadPICXML("C:/Program Files (x86)/Microchip/MPLABX/vDefault/", partNumber);

        if(doc == null)
        {
            System.err.printf("Could not load crownking EDC file for %1$s\n", partNumber);
            System.exit(-1);
        }

        NodeList sfrNodes = doc.getElementsByTagName("edc:SFRDef");
        NodeList dcrNodes = doc.getElementsByTagName("edc:DCRDef");
        Map<String, EZBL_SFR> SFRMap = IndexSFRNodes(sfrNodes);
        Map<String, EZBL_SFR> DCRMap = IndexSFRNodes(dcrNodes);
        List<EZBL_SFR> sortedSFRs = new ArrayList<EZBL_SFR>();
        sortedSFRs.addAll(SFRMap.values());
        Collections.sort(sortedSFRs);
        List<EZBL_SFR> sortedDCRs = new ArrayList<EZBL_SFR>();
        sortedSFRs.addAll(DCRMap.values());
        Collections.sort(sortedDCRs);

        for(EZBL_SFR sfr : sortedSFRs)
        {
            hStrings.add(String.format("\n#define ADDR_%1$-15s 0x%2$04X // to 0x%3$04X; Module: %4$s", sfr.name, sfr.addr, sfr.endAddr, sfr.srcModule.replace(".Module", "")));
            asmStrings.add(String.format("\n_%1$-15s = 0x%2$04X", sfr.name, sfr.addr));
            asmStrings.add(String.format("\n\t.global   _%1$s", sfr.name));
            for(BitField bf : sfr.bitfieldByName.values())
            {
                String macroName = sfr.name + "_" + bf.name + "_POS";
                hStrings.add(String.format("\n#define %1$-20s %2$-2d     // to %3$2d", macroName, bf.position, bf.position + bf.width));
            }
        }
        for(EZBL_SFR sfr : sortedDCRs)
        {
            hStrings.add(String.format("\n#define ADDR_%1$-15s 0x%2$06X   // to 0x%3$06X; Module: %4$s", sfr.name, sfr.addr, sfr.endAddr, sfr.srcModule.replace(".Module", "")));
            for(BitField bf : sfr.bitfieldByName.values())
            {
                String macroName = sfr.name + "_" + bf.name + "_POS";
                hStrings.add(String.format("\n#define %1$-20s %2$-2d     // to %3$2d; %4$s", macroName, bf.position, bf.position + bf.width, bf.desc));
            }
        }

        // Build a list of pins and what functions are multiplexed on each of them
        NodeList pinLists = doc.getElementsByTagName("edc:PinList");
        if(pinLists.getLength() != 0)
        {
            NodeList pinList = pinLists.item(0).getChildNodes();

            int pinNumber = 0;
            for(int i = 0; i < pinList.getLength(); i++)
            {
                Node pin = pinList.item(i);
                if(!pin.getNodeName().equals("edc:Pin"))
                {
                    continue;
                }
                pinNumber++;
                int functionNumber = 0;
                NodeList virtualPins = pin.getChildNodes();
                if(virtualPins.getLength() == 0)
                {
                    continue;
                }
                for(int j = 0; j < virtualPins.getLength(); j++)
                {
                    Node virtualPin = virtualPins.item(j);
                    String funcMneumonic;

                    if(!virtualPin.getNodeName().equals("edc:VirtualPin"))
                    {
                        continue;
                    }
                    functionNumber++;
                    funcMneumonic = virtualPin.getAttributes().getNamedItem("edc:name").getNodeValue();
                    pinNumberFunctionMappings.add(new PairWithText(pinNumber, functionNumber, funcMneumonic));
                }
            }
        }

        // Build a list of RP output pin to peripheral signal encodings
        NodeList virtualPins = doc.getElementsByTagName("edc:VirtualPin");
        for(int i = 0; i < virtualPins.getLength(); i++)
        {
            Node n = virtualPins.item(i);
            if(n.hasAttributes())
            {
                NamedNodeMap attr = n.getAttributes();
                Node name = attr.getNamedItem("edc:name");
                Node RPOutVal = attr.getNamedItem("edc:ppsval");
                if((name != null) && (RPOutVal != null))
                {
                    RPOutEncodings.add(new PairWithText(Long.decode(RPOutVal.getNodeValue()), name.getNodeValue()));
                }
            }
        }

        fileOutContents = "// THIS IS AN AUTO-GENERATED FILE!"
                          + "\n// Created for: " + partNumber
                          + "\n"
                          + "\n#ifndef PIC_ABSTRACTION_H"
                          + "\n#define PIC_ABSTRACTION_H"
                          + "\n\n" + Multifunction.CatStringList(hStrings)
                          + "\n"
                          + "\n"
                          + "\n // I/O pins and their named function mappings"
                          + "\n //     Pin Function     Pin Number             All pin functions";
        for(int i = 0; i < pinNumberFunctionMappings.size(); i++)
        {
            PairWithText mapping = pinNumberFunctionMappings.get(i);
            String allFunctionComment = "";
            for(PairWithText mapping2 : pinNumberFunctionMappings)
            {
                if(mapping.first != mapping2.first)
                {
                    continue;
                }
                if(!mapping.equals(mapping2) && mapping.text.equals(mapping2.text))
                {
                    mapping.text = mapping.text + String.format("_AT_%1$d", mapping.first);
                }
                allFunctionComment += mapping2.text + "/";
            }
            allFunctionComment = allFunctionComment.substring(0, allFunctionComment.length() - 1);
            mapping.text = mapping.text.replace("+", "p");
            mapping.text = mapping.text.replace("-", "n");
            fileOutContents += String.format("\n#define %1$-16s %2$3d\t// Pin %2$3d: %3$s", mapping.text, mapping.first, allFunctionComment);
        }

        fileOutContents += "\n"
                           + "\n"
                           + "\n // Remappable Peripheral Output encodings";
        for(PairWithText mapping : RPOutEncodings)
        {
            fileOutContents += String.format("\n#define RPOUT_%1$-16s 0x%2$02Xu", mapping.text, mapping.first);
        }

        fileOutContents += "\n"
                           + "\n#endif  // #ifndef PIC_ABSTRACTION_H"
                           + "\n";

        Multifunction.UpdateFileIfDataDifferent(outputFilename, fileOutContents, true);

        return ret;

    }
}


/**
 * Compares PIC names by alphabetical name, compensating for internal number
 * fields (like flash size) that are not the same width
 */
class PICNameComparator implements Comparator<String>
{
    @Override
    public int compare(String x, String y)
    {
        if(x.equals(y))
        {
            return 0;
        }

        if(x.endsWith("*") && !y.endsWith("*"))
        {
            return -1;
        }
        if(y.endsWith("*") && !x.endsWith("*"))
        {
            return 1;
        }
        if(x.endsWith(" family") && !y.endsWith(" family"))
        {
            return -1;
        }
        if(y.endsWith(" family") && !x.endsWith(" family"))
        {
            return 1;
        }

        String[] devClass = new String[2];
        String[] memSize = new String[2];
        Integer[] memSizeInt = new Integer[2];
        String[] featureSet = new String[2];
        String[] featureSetNum = new String[2];
        String[] pinoutCode = new String[2];
        Integer[] pinoutCodeInt = new Integer[2];
        String[] suffix = new String[2];
        Pattern p = Pattern.compile("(?<devClass>(dsPIC30|dsPIC33|PIC24)[^0-9]+)(?<memSize>[0-9]*)(?<featureSet>[a-zA-Z]*)(?<featureSetNum>[0-9]{0,1})(?<pinoutCode>[0-9]*)(?<suffix>.*)", Pattern.DOTALL);
        Matcher m;

        for(int i = 0; i < 2; i++)
        {
            String s = i == 0 ? x : y;
            m = p.matcher(s);
            if(!m.find())
            {
                return x.compareTo(y);
            }
            devClass[i] = m.group("devClass");
            memSize[i] = m.group("memSize");
            try
            {
                memSizeInt[i] = Integer.decode(memSize[i]);
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                memSizeInt[i] = -1;
            }
            featureSet[i] = m.group("featureSet");
            featureSetNum[i] = m.group("featureSetNum");
            pinoutCode[i] = m.group("pinoutCode");
            try
            {
                pinoutCodeInt[i] = Integer.parseInt(pinoutCode[i]);
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                pinoutCodeInt[i] = -1;
            }
            suffix[i] = m.group("suffix");
        }
        if(((devClass[0] == null) || (devClass[1] == null)) || !devClass[0].equals(devClass[1]))
        {
            if(devClass[0].startsWith("dsPIC") && !devClass[1].startsWith("dsPIC"))
            {
                return -1;
            }
            if(devClass[1].startsWith("dsPIC") && !devClass[0].startsWith("dsPIC"))
            {
                return 1;
            }

            return x.compareTo(y);
        }

        if(!featureSet[1].equals(featureSet[0]))
        {
            return featureSet[1].compareTo(featureSet[0]);
        }
        if(!memSize[1].equals(memSize[0]))
        {
            return memSizeInt[1].compareTo(memSizeInt[0]);
        }
        if(!pinoutCode[1].equals(pinoutCode[0]))
        {
            return pinoutCodeInt[1].compareTo(pinoutCodeInt[0]);
        }
        if(!featureSetNum[1].equals(featureSetNum[0]))
        {
            return featureSetNum[1].compareTo(featureSetNum[0]);
        }
        if(!suffix[0].equals(suffix[1]))
        {
            return suffix[0].compareTo(suffix[1]);
        }
        return x.compareTo(y);
    }

}


/**
 * Compares SFR/DCR/Bitfield names by alphabetical name, compensating for
 * internal number fields
 */
class SFRNameComparator implements Comparator<String>
{

    @Override
    public int compare(String x, String y)
    {
        if((x == null) && (y != null))
        {
            return -1;
        }
        if((y == null) && (x != null))
        {
            return 1;
        }
        if(x == null)
        {
            return 0;
        }
        if(y == null)
        {
            return 0;
        }
        if(x.equals(y))
        {
            return 0;
        }
        if(x.isEmpty() && !y.isEmpty())
        {
            return -1;
        }
        if(y.isEmpty() && !x.isEmpty())
        {
            return 1;
        }

        String tokenx, tokeny;
        int numx, numy;
        Pattern p = Pattern.compile("([^0-9]+)([0-9]*)", Pattern.DOTALL);
        Matcher mx, my;

        mx = p.matcher(x);
        my = p.matcher(y);
        while(mx.find() && my.find())
        {
            tokenx = mx.group(1);
            tokeny = my.group(1);
            if(!tokenx.equals(tokeny))
            {
                return tokenx.compareTo(tokeny);
            }
            numx = Integer.parseInt(mx.group(2));
            numy = Integer.parseInt(my.group(2));
            if(numx < numy)
            {
                return -1;
            }
            if(numx > numy)
            {
                return 1;
            }
        }
        return x.compareTo(y);
    }

}


class DecodedNode
{
    Node node;
    String tagName;
    LinkedHashMap<String, String> attribs = new LinkedHashMap<String, String>();    // Attribute name, Attribute value as String
    LinkedHashMap<String, DecodedNode> children = null;

    public DecodedNode()
    {
        node = null;
        tagName = null;
    }

    public DecodedNode(Node node)
    {
        this.node = node;
        this.tagName = node.getNodeName();
        NamedNodeMap attribs = node.getAttributes();
        if(attribs != null)
        {
            for(int i = 0; i < attribs.getLength(); i++)
            {
                String val = attribs.item(i).getNodeValue();
                if(val == null)
                    val = "";
                this.attribs.put(attribs.item(i).getNodeName(), val);
            }
        }
    }

    public Integer getAttrAsInt(String attribName)
    {
        if(!this.attribs.containsKey(attribName))
            return null;
        return Integer.decode(this.attribs.get(attribName));
    }
    public int getAttrAsInt(String attribName, int defaultValue)
    {
        if(!this.attribs.containsKey(attribName))
            return defaultValue;
        return Integer.decode(this.attribs.get(attribName));
    }
    public Long getAttrAsLong(String attribName)
    {
        if(!this.attribs.containsKey(attribName))
            return null;
        return Long.decode(this.attribs.get(attribName));
    }
    public long getAttrAsLong(String attribName, long defaultValue)
    {
        if(!this.attribs.containsKey(attribName))
            return defaultValue;
        return Long.decode(this.attribs.get(attribName));
    }
    public String getAttrAsString(String attribName, String defaultValue)
    {
        if(!this.attribs.containsKey(attribName))
            return defaultValue;
        return this.attribs.get(attribName);
    }
    public String getAttrAsString(String attribName)    // Returns null if the named attribute doesn't exist
    {
        return this.attribs.get(attribName);
    }

    public String toString()
    {
        List<String> ret = new ArrayList<>();
        ret.add("<" + this.tagName);
        for(String key : this.attribs.keySet())
        {
            ret.add(key + "=" + attribs.get(key));
        }
        return CatStringList(ret, " ") + ">";
    }
}


class DecodedNodeList
{
    List<DecodedNode> list = new ArrayList<>();
    DecodedNode first = null;
    DecodedNode firstDefault = null;

    public DecodedNodeList()
    {
    }
    public DecodedNodeList(boolean setFirstDefault)
    {
        if(setFirstDefault)
            firstDefault = new DecodedNode();
    }
    public DecodedNodeList(DecodedNode firstDecodedNode)
    {
        list.add(first);
        firstDefault = firstDecodedNode;
        first = firstDecodedNode;
    }
    public void putAll(List<DecodedNode> decodedNodes)
    {
        list.addAll(decodedNodes);
    }
    public void putAll(DecodedNodeList decodedNodes)
    {
        list.addAll(decodedNodes.list);
    }
    public void put(DecodedNode decodedNode)
    {
        list.add(decodedNode);
    }
    public void put(Node node)
    {
        list.add(new DecodedNode(node));
    }
}
