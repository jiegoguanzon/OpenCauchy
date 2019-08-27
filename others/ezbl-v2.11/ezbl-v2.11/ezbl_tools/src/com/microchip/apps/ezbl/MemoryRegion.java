/*
 *******************************************************************************
 * Copyright (c) 2018 Microchip Technology Inc. All rights reserved.
 *
 * Microchip licenses to you the right to use, modify, copy and distribute
 * Software only when embedded on a Microchip microcontroller or digital signal
 * controller that is integrated into your product or third party product
 * (pursuant to the sublicense terms in the accompanying license agreement).
 *
 * You should refer to the license agreement accompanying this Software for
 * additional information regarding your rights and obligations.
 *
 * SOFTWARE AND DOCUMENTATION ARE PROVIDED AS IS WITHOUT WARRANTY OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF
 * MERCHANTABILITY, TITLE, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR
 * PURPOSE. IN NO EVENT SHALL MICROCHIP OR ITS LICENSORS BE LIABLE OR OBLIGATED
 * UNDER CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF
 * WARRANTY, OR OTHER LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR
 * EXPENSES INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF
 * PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD
 * PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR
 * COSTS.
 *******************************************************************************
 */
package com.microchip.apps.ezbl;

import static com.microchip.apps.ezbl.Multifunction.TrimQuotes;
import java.io.*;
import java.util.*;
import java.util.regex.*;


/**
 *
 * @author C12128
 */
public class MemoryRegion extends AddressRange implements Serializable, Cloneable, Comparable<AddressRange>
{
    static final long serialVersionUID = 1L;
    public String name = null;
    public int attribR;   // Read-only                -1 = not set, 0 = absent, 1 = set
    public int attribW;   // Read-Write               -1 = not set, 0 = absent, 1 = set
    public int attribX;   // Executable               -1 = not set, 0 = absent, 1 = set
    public int attribA;   // Allocatable              -1 = not set, 0 = absent, 1 = set
    public int attribI;   // Initialized (also 'L')   -1 = not set, 0 = absent, 1 = set
    //public long startAddr;    // In super class
    //public long endAddr;      // In super class
    public String comment = null;
    public int programAlign;
    public int eraseAlign;


    public static enum Partition
    {
        single,
        partition1,
        partition2
    };
    public Partition partition = Partition.single;


    /**
     * All of the different types of memory on a device, each with special
     * alignment, erase/programming NVMOPs, erase/programming block sizes, write
     * once, ICSP only, etc. properties
     */
    public static enum MemType
    {
        DEBUG, // Also unknown
        SFR, // Data space, but SFR address range
        RAM, // RAM data memory
        ROM, // Ordinary flash, boot flash, auxiliary flash, or inactive partition flash memory
        FLASHFUSE,// Flash based Configuration words
        BYTEFUSE, // EEPROM-like Configuration bytes
        EEPROM, // EEPROM, non-execution memory
        OTP,
        TEST
    };
    public MemType type = MemType.DEBUG;

    public static boolean MemTypeIsProgSpace(MemType mem)
    {
        return mem.equals(MemType.BYTEFUSE) || mem.equals(MemType.EEPROM) || mem.equals(MemType.FLASHFUSE) || mem.equals(MemType.OTP) || mem.equals(MemType.ROM) || mem.equals(MemType.TEST);
    }
    public static boolean MemTypeIsDataSpace(MemType mem)
    {
        return mem.equals(MemType.RAM) || mem.equals(MemType.SFR);
    }
    public static boolean MemTypeIsDebugSpace(MemType mem)
    {
        return mem.equals(MemType.DEBUG);
    }
    public static boolean MemSpaceEqual(MemType mem1, MemType mem2) // True if mem1 and mem2 are both in ProgSpace, both in DataSpace, or both in Debug space
    {
        if(MemoryRegion.MemTypeIsDataSpace(mem1))
            return MemoryRegion.MemTypeIsDataSpace(mem2);
        if(MemoryRegion.MemTypeIsProgSpace(mem1))
            return MemoryRegion.MemTypeIsProgSpace(mem2);
        return MemoryRegion.MemTypeIsDebugSpace(mem2);
    }
    public boolean isProgSpace()
    {
        return MemoryRegion.MemTypeIsProgSpace(this.type);
    }
    public boolean isDataSpace()
    {
        return MemoryRegion.MemTypeIsDataSpace(this.type);
    }
    public boolean isDebugSpace()
    {
        return MemoryRegion.MemTypeIsDebugSpace(this.type);
    }
    public boolean memSpaceEqual(MemType mem)
    {
        return MemoryRegion.MemSpaceEqual(this.type, mem);
    }
    public boolean memSpaceEqual(MemoryRegion region)
    {
        return MemoryRegion.MemSpaceEqual(this.type, region.type);
    }

    @Override
    public MemoryRegion clone()
    {
        MemoryRegion ret = MemoryRegion.class.cast(super.clone());

        ret.name = this.name;
        ret.attribR = this.attribR;
        ret.attribW = this.attribW;
        ret.attribX = this.attribX;
        ret.attribA = this.attribA;
        ret.attribI = this.attribI;
        ret.comment = this.comment;
        ret.partition = this.partition;
        ret.type = this.type;
        ret.programAlign = this.programAlign;
        ret.eraseAlign = this.eraseAlign;

        return ret;
    }

    public MemoryRegion()
    {
    }

    public MemoryRegion(long startAddr, long endAddr)
    {
        this.startAddr = startAddr;
        this.endAddr = endAddr;
    }

    public MemoryRegion(AddressRange ar)
    {
        this.startAddr = ar.startAddr;
        this.endAddr = ar.endAddr;
    }

    public MemoryRegion(String gldLine)
    {
        String parseTemp;
        Long multiplier;

        //   data  (a!xr)   : ORIGIN = 0x1000,        LENGTH = 0x1000
        gldLine = gldLine.replaceAll("[\\s]", "");  // Remove all whitespace
        //data(a!xr):ORIGIN=0x1000,LENGTH=0x1000
        Pattern p = Pattern.compile("([\\w]+\\b)([(].*?[)])??[:].*?[=]([0-9xXa-fA-F]+[MKmk]??)[,].*?[=]([0-9xXa-fA-F]+[MKmk]??)", Pattern.DOTALL);
        Matcher m = p.matcher(gldLine);
        if(m.find() == false)
        {
            return;
        }
        name = m.group(1);
        parseTemp = m.group(2); // Get attributes and decode them
        if(parseTemp != null)
        {
            parseTemp = parseTemp.toUpperCase();
            if(parseTemp.matches(".*R.*[!].*"))    // Not Read-only
            {
                attribR = -1;
            }
            else if(parseTemp.matches(".*R.*"))    // Read-only
            {
                attribR = 1;
            }
            if(parseTemp.matches(".*W.*[!].*"))    // Not Writeable
            {
                attribW = -1;
            }
            else if(parseTemp.matches(".*W.*"))    // Writable
            {
                attribW = 1;
            }
            if(parseTemp.matches(".*X.*[!].*"))    // Not Executable
            {
                attribX = -1;
            }
            else if(parseTemp.matches(".*X.*"))    // Executable
            {
                attribX = 1;
            }
            if(parseTemp.matches(".*A.*[!].*"))    // Not Allocatable (loadable)
            {
                attribA = -1;
            }
            else if(parseTemp.matches(".*A.*"))    // Allocatable (loadable)
            {
                attribA = 1;
            }
            if(parseTemp.matches(".*(I|L).*[!].*"))    // Not Initialized
            {
                attribI = -1;
            }
            else if(parseTemp.matches(".*(I|L).*"))    // Initialized
            {
                attribI = 1;
            }
        }

        // Decode ORIGIN and LENGTH fields
        for(int i = 3; i <= 4; i++)
        {
            parseTemp = m.group(i).toUpperCase();
            if(parseTemp.contains("M"))
            {
                multiplier = 1024L * 1024L;
                parseTemp = parseTemp.substring(0, parseTemp.length() - 1);
            }
            else if(parseTemp.contains("K"))
            {
                multiplier = 1024L;
                parseTemp = parseTemp.substring(0, parseTemp.length() - 1);
            }
            else
            {
                multiplier = 1L;
            }

            if(i == 3)
                startAddr = Long.decode(parseTemp) * multiplier;
            else
                endAddr = startAddr + Long.decode(parseTemp) * multiplier;
        }
    }

    public static MemoryRegion fromCSVLine(String csvFieldData[])
    {
        MemoryRegion mr = new MemoryRegion();
        // id INTEGER
        // part TEXT
        // type INTEGER     MemoryRegion.MemType enum ordinal
        // partition INTEGER MemoryRegion.Partition enum ordinal
        // name TEXT
        // startAddr INTEGER
        // endAddr INTEGER
        // programAlign INTEGER
        // eraseAlign INTEGER
        int i = 2;
        mr.type = MemType.values()[Integer.decode(csvFieldData[i++])];
        mr.partition = Partition.values()[Integer.decode(csvFieldData[i++])];
        mr.name = csvFieldData[i++].isEmpty() ? null : TrimQuotes(csvFieldData[i - 1]);
        mr.startAddr = Long.decode(csvFieldData[i++]);
        mr.endAddr = Long.decode(csvFieldData[i++]);
        mr.programAlign = Integer.decode(csvFieldData[i++]);
        mr.eraseAlign = Integer.decode(csvFieldData[i++]);
        return mr;
    }

    public static MemoryRegion fromCSVLine(String csvData)
    {
        return fromCSVLine(csvData.split("[,]"));
    }

    /**
     * Copies all members from the referenceRegion except the startAddress and
     * endAddress, and optionally, the name and comment fields.
     */
    public MemoryRegion copyMetaData(MemoryRegion referenceRegion, boolean includeName, boolean includeComment)
    {
        this.attribA = referenceRegion.attribA;
        this.attribI = referenceRegion.attribI;
        this.attribR = referenceRegion.attribR;
        this.attribW = referenceRegion.attribW;
        this.attribX = referenceRegion.attribX;
        this.eraseAlign = referenceRegion.eraseAlign;
        if(includeComment)
            this.comment = referenceRegion.comment;
        if(includeName)
            this.name = referenceRegion.name;
        this.partition = referenceRegion.partition;
        this.programAlign = referenceRegion.programAlign;
        this.type = referenceRegion.type;
        return this;
    }

    /**
     * Copies all members from the referenceRegion except the startAddress and
     * endAddress, and optionally, the name and comment fields.
     */
    public MemoryRegion copyMetaData(MemoryRegion referenceRegion)
    {
        return copyMetaData(referenceRegion, true, true);
    }

    /**
     * Tests if the two regions are overlapped, and if so, computes the boundary
     * addresses of the overlapped region.
     *
     * @param y Second memory region to compare with this.
     *
     * @return null if no overlap exists. If overlap does exist, a clone of this
     *         region is made (or the y region, if y is entirely enclosed within
     *         or matched with this) and the startAddr and endAddr parameters
     *         are updated to reflect the overlapped region.
     */
    public MemoryRegion getOverlapWith(MemoryRegion y)
    {
        if((y == null) || (this.startAddr >= y.endAddr) || (this.endAddr <= y.startAddr))
            return null;

        MemoryRegion ret = this.clone();
        if((this.startAddr <= y.startAddr) && (this.endAddr >= y.endAddr))
            ret = y.clone();

        if(this.startAddr < y.startAddr)
            ret.startAddr = y.startAddr;
        if(this.endAddr > y.endAddr)
            ret.endAddr = y.endAddr;
        return ret;
    }

    /**
     * Aligns the startAddr and endAddr fields of the MemoryRegion to the lessor
     * of the given address block size or any of the provided overlapTestRegions
     *
     * @param alignAddrSize      Address block size to align the start and end
     *                           addresses to, assuming no overlap.
     * @param overlapTestRegions List of MemoryRegions to compare the new
     *                           aligned startAddr and endAddr to. If either
     *                           address extends into an overlapTestRegion when
     *                           it didn't initially, the address is instead
     *                           aligned to meet the overlap boundary instead.
     *
     * If this parameter is null or an empty list, the alignment is carried out
     * exclusively using the alignAddrSize parameter.
     *
     * @return Reference to this MemoryRegion, now aligned.
     */
    public MemoryRegion align(int alignAddrSize, List<MemoryRegion> overlapTestRegions)
    {
        if(alignAddrSize == 0)
            return this;

        long originalStart = startAddr;
        long originalEnd = endAddr;

        startAddr -= startAddr % alignAddrSize;
        long mod = endAddr % alignAddrSize;
        if(mod != 0)
            endAddr += alignAddrSize - mod;

        if((overlapTestRegions == null) || overlapTestRegions.isEmpty())
            return this;

        for(MemoryRegion boundaryRegion : overlapTestRegions)
        {
            if((originalStart >= boundaryRegion.endAddr) && (startAddr < boundaryRegion.endAddr))
                startAddr = boundaryRegion.endAddr;
            if((originalEnd <= boundaryRegion.startAddr) && (endAddr > boundaryRegion.startAddr))
                endAddr = boundaryRegion.startAddr;
            if((originalStart >= boundaryRegion.startAddr) && (startAddr < boundaryRegion.startAddr))
                startAddr = boundaryRegion.startAddr;
            if((originalEnd <= boundaryRegion.endAddr) && (endAddr > boundaryRegion.endAddr))
                endAddr = boundaryRegion.endAddr;
        }
        return this;
    }
    public MemoryRegion align(int alignAddrSize)
    {
        return align(alignAddrSize, null);
    }

    public static List<MemoryRegion> align(List<MemoryRegion> regions, int alignAddrSize)
    {
        return align(regions, alignAddrSize, null);
    }
    public static List<MemoryRegion> align(List<MemoryRegion> regions, int alignAddrSize, List<MemoryRegion> overlapTestRegions)
    {
        if(regions == null)
            return null;
        if(alignAddrSize == 0)
            return regions;
        for(MemoryRegion r : regions)
        {
            r.align(alignAddrSize, overlapTestRegions);
        }
        return regions;
    }

    public static List<MemoryRegion> alignToProgSize(List<MemoryRegion> regions, List<MemoryRegion> overlapTestRegions)
    {
        for(MemoryRegion r : regions)
        {
            r.align(r.programAlign, overlapTestRegions);
        }
        return regions;
    }
    public static List<MemoryRegion> alignToProgSize(List<MemoryRegion> regions)
    {
        return alignToProgSize(regions, null);
    }

    public static List<MemoryRegion> alignToEraseSize(List<MemoryRegion> regions, List<MemoryRegion> overlapTestRegions)
    {
        for(MemoryRegion r : regions)
        {
            r.align(r.eraseAlign, overlapTestRegions);
        }
        return regions;
    }

    public static List<MemoryRegion> alignToEraseSize(List<MemoryRegion> regions)
    {
        return alignToEraseSize(regions, null);
    }

    public MemoryRegion alignToProgSizeWithoutOverlap(List<MemoryRegion> overlapTestRegions)
    {
        return this.align(this.programAlign, overlapTestRegions);
    }
    public MemoryRegion alignToProgSize()
    {
        return this.align(this.programAlign, null);
    }

    public MemoryRegion alignToEraseSizeWithoutOverlap(List<MemoryRegion> overlapTestRegions)
    {
        return this.align(this.eraseAlign, overlapTestRegions);
    }
    public MemoryRegion alignToEraseSize()
    {
        return this.align(this.eraseAlign, null);
    }

    public static void SubtractRegion(List<MemoryRegion> regionList, MemoryRegion subtractRegion)
    {
        for(int i = 0; i < regionList.size(); i++)
        {
            MemoryRegion region = regionList.get(i);
            if(!region.memSpaceEqual(subtractRegion))
                continue;
            if((subtractRegion.endAddr > region.startAddr) && (subtractRegion.startAddr < region.endAddr))    // Overlap exists
            {
                if((subtractRegion.startAddr >= region.startAddr) && (subtractRegion.endAddr < region.endAddr))
                {   // Lies in the middle -> must split reference range into two ranges
                    MemoryRegion rightRegion = region.clone();
                    rightRegion.startAddr = subtractRegion.endAddr;
                    regionList.add(i + 1, rightRegion);
                    region.endAddr = subtractRegion.startAddr;
                }
                else if((subtractRegion.startAddr >= region.startAddr) && (subtractRegion.endAddr >= region.endAddr))
                {   // Overlaps with right half -> trim the right edge off the reference range
                    region.endAddr = subtractRegion.startAddr;
                }
                else if((subtractRegion.startAddr <= region.startAddr) && (subtractRegion.endAddr > region.startAddr))
                {   // Overlaps with left half -> trim the left edge off the reference range
                    region.startAddr = subtractRegion.endAddr;
                }
                if(region.startAddr >= region.endAddr)
                {
                    regionList.remove(i--);
                }
            }
        }
    }

    public static List<MemoryRegion> SubtractRegions(List<MemoryRegion> regionList, MemoryRegion subtractRegion, boolean strictMemTypeMatchRequired)
    {
        return SubtractRegions(regionList, subtractRegion.getAsList(), strictMemTypeMatchRequired);
    }

    // Same as SubtractRegions(List<MemoryRegion> referenceRegionList, List<MemoryRegion> subtractRegionList), but starting with only one region to trim against and returning a 0 width region or only the first region after trimming, discarding any extras
    public static List<MemoryRegion> SubtractRegions(MemoryRegion referenceRegion, List<MemoryRegion> subtractRegionList, boolean strictMemTypeMatchRequired)
    {
        return SubtractRegions(referenceRegion.getAsList(), subtractRegionList, strictMemTypeMatchRequired);
    }

    /**
     * Returns the cloned logical subtraction of two memory region lists
     *
     * @param referenceRegionList  Starting list of memory regions
     * @param subtractRegionList   Regions to subtract from referenceRegionList
     * @param memTypeMatchRequired True if overlapped ranges require a matching
     *                             memory type as well as address. False if
     *                             overlapped ranges are computed only against
     *                             region start and end addresses.
     *
     * @return Cloned copy of (referenceRegionList - subtractRegionList) with
     *         any zero length regions purged.
     */
    public static List<MemoryRegion> SubtractRegions(List<MemoryRegion> referenceRegionList, List<MemoryRegion> subtractRegionList, boolean memTypeMatchRequired)
    {
        List<MemoryRegion> ret;

        ret = MemoryRegion.coalesce(referenceRegionList, 0, 0, memTypeMatchRequired); // Clone and simplify work
        if((subtractRegionList == null) || subtractRegionList.isEmpty())
            return ret;

        for(int i = 0; i < ret.size(); i++)
        {
            MemoryRegion ref = ret.get(i);
            for(int j = 0; j < subtractRegionList.size(); j++)
            {
                MemoryRegion sub = subtractRegionList.get(j);
                if(memTypeMatchRequired && (ref.type != sub.type))
                    continue;
                if(!ref.memSpaceEqual(sub))
                    continue;
                if((sub.endAddr > ref.startAddr) && (sub.startAddr < ref.endAddr))    // Overlap exists
                {
                    if((sub.startAddr >= ref.startAddr) && (sub.endAddr < ref.endAddr))
                    {   // Lies in the middle -> must split reference range into two ranges                        
                        MemoryRegion rightRegion = ref.clone();
                        rightRegion.startAddr = sub.endAddr;
                        ret.add(i + 1, rightRegion);
                        ref.endAddr = sub.startAddr;
                    }
                    else if((sub.startAddr >= ref.startAddr) && (sub.endAddr >= ref.endAddr))
                    {   // Overlaps with right half -> trim the right edge off the reference range
                        ref.endAddr = sub.startAddr;
                    }
                    else if((sub.startAddr <= ref.startAddr) && (sub.endAddr > ref.startAddr))
                    {   // Overlaps with left half -> trim the left edge off the reference range
                        ref.startAddr = sub.endAddr;
                    }
                }
                if(ref.startAddr >= ref.endAddr)    // Throw reference record array if it now has a 0 or negative size
                {
                    ret.remove(i--);
                    break;
                }
            }
        }

        return ret;
    }

    public static List<MemoryRegion> coalesce(List<MemoryRegion> regions, boolean align, List<MemoryRegion> overlapTestRegions, boolean strictMemTypeMatchRequired)
    {
        List<MemoryRegion> ret = new ArrayList<>(regions.size());
        for(MemoryRegion mr : regions)
        {
            if(mr.startAddr == mr.endAddr)
                continue;
            MemoryRegion mr2 = mr.clone();
            if(align)
            {
                if(mr2.type == MemType.ROM)
                    mr2.align(mr2.eraseAlign, overlapTestRegions);
                else if((mr2.type == MemType.FLASHFUSE) || (mr2.type == MemType.BYTEFUSE) || (mr2.type == MemType.TEST))
                    mr2.align(mr2.programAlign, overlapTestRegions);
            }
            ret.add(mr2);
        }
        return coalesce(ret, 0, 0, strictMemTypeMatchRequired);
    }

    // Clones, sorts, left and right aligns, and coalesces overlapped regions (of the same type)
    public static List<MemoryRegion> coalesce(List<MemoryRegion> regions, int leftAlign, int rightAlign, boolean strictMemTypeMatchNeeded)
    {
        List<MemoryRegion> refList = new ArrayList<>();
        MemoryRegion mr;
        MemoryRegion mr2;

        for(int i = 0; i < regions.size(); i++)
        {
            refList.add(regions.get(i).clone());
        }

        if(leftAlign == 0)
            leftAlign = 1;
        if(rightAlign == 0)
            rightAlign = 1;
        Collections.sort(refList);

        // Iterate over all records in refList and coalesce with left and right padding added as needed
        for(int i = 0; i < refList.size() - 1; i++)
        {
            mr = refList.get(i);
            if(mr.startAddr == mr.endAddr)
            {
                refList.remove(i--);
                continue;
            }
            mr.startAddr -= mr.startAddr % leftAlign;     // Left align
            mr.endAddr += (mr.endAddr % rightAlign == 0) ? 0 : rightAlign - ((mr.endAddr + rightAlign) % rightAlign);
            for(int j = i + 1; j < refList.size(); j++)
            {
                mr2 = refList.get(j);
                if(strictMemTypeMatchNeeded && (mr.type != mr2.type))
                    continue;
                if(!mr.memSpaceEqual(mr2))
                    continue;
                mr2.startAddr -= mr2.startAddr % leftAlign;
                if(mr.endAddr < mr2.startAddr)    // Does right padded mr not abut or overlap left padded mr2?
                    break;
                if(mr2.endAddr > mr.endAddr)
                    mr.endAddr = mr2.endAddr;
                refList.remove(j--);
                mr.endAddr += (mr.endAddr % rightAlign == 0) ? 0 : rightAlign - ((mr.endAddr + rightAlign) % rightAlign);
            }
        }

        return refList;
    }

    // Clones the given regions, aligns the start and end addresses to the programAlign size, sorts them by start address, then combines any regions that overlap, extending the first and removing the later overlapped regions
    public static List<MemoryRegion> coalesce(List<MemoryRegion> regions, boolean align, boolean strictMemTypeMatchRequired)
    {
        return coalesce(regions, align, null, strictMemTypeMatchRequired);
    }

    public int compareTo(MemoryRegion y)
    {
        return this.startAddr == y.startAddr ? 0 : (this.startAddr > y.startAddr ? 1 : -1);
    }

    @Override
    public int compareTo(AddressRange y)
    {
        return this.startAddr == y.startAddr ? 0 : (this.startAddr > y.startAddr ? 1 : -1);
    }

    @Override
    public String toString()
    {
        String nameAttributes;
        String attributes;

        nameAttributes = "    " + name + " ";
        attributes = "";
        if(attribA == -1)
        {
            attributes += "a";
        }
        if(attribI == -1)
        {
            attributes += "i";
        }
        if(attribX == -1)
        {
            attributes += "x";
        }
        if(attribR == -1)
        {
            attributes += "r";
        }
        if(attribW == -1)
        {
            attributes += "w";
        }
        if(!attributes.isEmpty())
        {
            attributes += "!";
        }
        if(attribA == 1)
        {
            attributes += "a";
        }
        if(attribI == 1)
        {
            attributes += "i";
        }
        if(attribX == 1)
        {
            attributes += "x";
        }
        if(attribR == 1)
        {
            attributes += "r";
        }
        if(attribW == 1)
        {
            attributes += "w";
        }
        if(!attributes.isEmpty())
        {
            nameAttributes += "(" + attributes + ") ";
        }

        if(comment == null)
        {
            return String.format("%1$-33s: ORIGIN = 0x%2$06X, LENGTH = 0x%3$06X", nameAttributes, startAddr, endAddr - startAddr);
        }
        return String.format("%1$-33s: ORIGIN = 0x%2$06X, LENGTH = 0x%3$06X    /* %4$s */", nameAttributes, startAddr, endAddr - startAddr, comment);
    }

    public String toDebugString()
    {
        return String.format("[%06X, %06X): %s %s %s progAlign=0x%X, eraseAlign=0x%X\n", this.startAddr, this.endAddr, this.partition.toString(), this.type.toString(), this.name, this.programAlign, this.eraseAlign);
    }

    /**
     * Generates a .gld or .ld linker script input SECTION to output SECTION
     * mapping for Bootloader reserved regions. Example text returned:
     * <code>
     *       EZBL_ROM_AT_0xBFC00000 0xBFC00000 :
     *       {
     *         *(EZBL_ROM_AT_0xBFC00000); /* [0xBFC00000, 0xBFC01700), contains 5888 bytes *\/
     *       } > kseg1_boot_mem
     * </code>
     *
     * @param extraAttributes Strings to add to the section mapping, such as:
     *                        "AT 0x1234"
     *
     * @return Generated .gld/.ld section mapping
     */
    public String toLinkerString(String... extraAttributes)
    {
        List<String> ret = new ArrayList<>();
        String secTypeName = isDataSpace() ? "RAM" : (isProgSpace() ? "ROM" : "DBG");
        String secName = String.format("EZBL_%s_AT_0x%04X", secTypeName, startAddr);
        if((type == MemType.BYTEFUSE) || (type == MemType.FLASHFUSE))
            secName = "EZBL_BTLDR_CONFIG_WORD_" + name;
        long byteSize = endAddr - startAddr;
        if(isProgSpace() && ((endAddr & 0xFF000000L) == 0L))
            byteSize = byteSize / 2 * 3;

        if(extraAttributes == null)
            extraAttributes = new String[0];
        String earlyAttribs = "";
        String lateAttribs = "";
        for(String attrib : extraAttributes)
        {
            if(attrib.toUpperCase().startsWith("AT"))
                lateAttribs += " " + attrib;
            else
                earlyAttribs += attrib + " ";
        }

        ret.add(String.format("%s 0x%04X %s:%s", secName, startAddr, earlyAttribs, lateAttribs));
        ret.add("{");
        ret.add(String.format("  *(%s); /* [0x%04X, 0x%04X), contains %d bytes */", secName, startAddr, endAddr, byteSize));
        ret.add(String.format("} > %s", name));
        return "\n  " + Multifunction.CatStringList(ret, "\n  ");
    }
    public String toLinkerString()
    {
        return toLinkerString(new String[0]);
    }

    /**
     * Generates a .S ASM30/ASM33 section definition reserving space for the
     * MemoryRegion.
     * <code>
     * </code>
     *
     * @param extraAttributes Strings to add to the section mapping, such as:
     *                        "keep", "noload", etc.
     *
     * @return Generated ASM .s code spanning the MemoryRegion
     */
    public String toASMString(String extraAttributes)
    {
        List<String> ret = new ArrayList<>();
        String secTypeName = isDataSpace() ? "RAM" : (isProgSpace() ? "ROM" : "DBG");
        String secName = String.format("EZBL_%s_AT_0x%04X", secTypeName, startAddr);
        String secAsmType = isDataSpace() ? "persist" : isProgSpace() ? "code" : "info";
        if((type == MemType.BYTEFUSE) || (type == MemType.FLASHFUSE))
            secName = "EZBL_BTLDR_CONFIG_WORD_" + name;
        if(extraAttributes == null)
            extraAttributes = "";
        if(!extraAttributes.isEmpty())
            extraAttributes += ", ";
        long byteSize = endAddr - startAddr;
        if(isProgSpace() && ((endAddr & 0xFF000000L) == 0L))
            byteSize = byteSize / 2 * 3;

        ret.add(String.format("; Bootloader %s block intended for '%s' region", secTypeName, name));
        ret.add(String.format("; 0x%04X to 0x%04X, length 0x%04X (%d bytes)", startAddr, endAddr, endAddr - startAddr, byteSize));
        ret.add(String.format(".pushsection    %s, address(0x%04X), %s, %skeep", secName, startAddr, secAsmType, extraAttributes));
        ret.add(String.format(".space      0x%X", endAddr - startAddr));
        ret.add(String.format(".popsection"));
        return "\n    " + Multifunction.CatStringList(ret, "\n    ");
    }
    public String toASMString()
    {
        return toASMString(null);
    }

    void normalizePIC32Addresses()
    {
        if(((this.type == MemType.RAM) && ((this.startAddr & 0xFFC00000L) == 0x00000000L))
           || // Convert RAM physical to kseg0 cached addresses
                (((this.type == MemType.ROM) || (this.type == MemType.FLASHFUSE)) && ((this.startAddr & 0x7FC00000L) == 0x1D000000L))) // Convert main flash to kseg0 cached addresses
        {
            this.startAddr |= 0x80000000L;    // Out = 0x9D000000 or 0x80000000
            this.endAddr |= 0x80000000L;
        }
        if((((this.type == MemType.ROM) || (this.type == MemType.FLASHFUSE)) && ((this.startAddr & 0x7FC00000L) == 0x1FC00000L))// Convert boot flash/config word physical to kseg1 uncached addresses
           || ((this.type == MemType.SFR) && ((this.startAddr & 0x7FC00000L) == 0x1F800000L)))// Convert SFR anything to kseg1 uncached addresses
        {
            this.startAddr |= 0xA0000000L;    // Out = 0xBDCxxxxx or 0xBF8xxxxx range
            this.endAddr |= 0xA0000000L;
        }
    }

    public List<MemoryRegion> getAsList()
    {
        List<MemoryRegion> ret = new ArrayList<>(1);
        ret.add(this);
        return ret;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getAttribR()
    {
        return attribR;
    }

    public void setAttribR(int attribR)
    {
        this.attribR = attribR;
    }

    public int getAttribW()
    {
        return attribW;
    }

    public void setAttribW(int attribW)
    {
        this.attribW = attribW;
    }

    public int getAttribX()
    {
        return attribX;
    }

    public void setAttribX(int attribX)
    {
        this.attribX = attribX;
    }

    public int getAttribA()
    {
        return attribA;
    }

    public void setAttribA(int attribA)
    {
        this.attribA = attribA;
    }

    public int getAttribI()
    {
        return attribI;
    }

    public void setAttribI(int attribI)
    {
        this.attribI = attribI;
    }

    public long getStartAddr()
    {
        return startAddr;
    }

    public void setStartAddr(long startAddr)
    {
        this.startAddr = startAddr;
    }

    public long getEndAddr()
    {
        return endAddr;
    }

    public void setEndAddr(long endAddr)
    {
        this.endAddr = endAddr;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }
}
