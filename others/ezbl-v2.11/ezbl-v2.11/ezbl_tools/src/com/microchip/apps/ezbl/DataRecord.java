/*
 *******************************************************************************
 * Copyright (c) 2017 Microchip Technology Inc. All rights reserved.
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

import com.microchip.apps.ezbl.MemoryRegion.MemType;
import com.microchip.apps.ezbl.MemoryRegion.Partition;
import static com.microchip.apps.ezbl.Multifunction.*;
import java.io.*;
import java.util.*;


/**
 *
 * @author C12128
 */
public class DataRecord implements Serializable, Cloneable, Comparator<DataRecord>, Comparable<DataRecord>
{

    public boolean architecture16Bit = false;
    public long address = -1;
    public byte data[] = null;
    public String assignedMemory = "";
    public String comment = "";

    public DataRecord Clone()
    {
        DataRecord ret = new DataRecord();

        ret.address = this.address;
        ret.architecture16Bit = this.architecture16Bit;
        ret.assignedMemory = this.assignedMemory;
        ret.comment = this.comment;
        if(this.data == null)
        {
            ret.data = null;
        }
        else
        {
            ret.data = Arrays.copyOf(this.data, this.data.length);
        }

        return ret;
    }

    public DataRecord()
    {
    }

    public DataRecord(boolean architchture16Bit)
    {
        this.architecture16Bit = architchture16Bit;
    }

    public DataRecord(long recordAddress, byte recordData[], boolean architecture16Bit)
    {
        address = recordAddress;
        data = recordData;
        this.architecture16Bit = architecture16Bit;
    }

    /**
     * Creates a DataRecord from 1 or more lines of text dumped with
     * xc16-objdump -s. All lines must have a '\n' terminator, even if there is
     * only one line. When multiple lines are given, the data must lie at
     * contiguous addresses.
     *
     * @param elfDumpSectionDataLines String of obj-dump output data for the
     *                                section, not including the section name.
     *                                Ex: 0000 240204 000000 $.....
     */
    public DataRecord(String elfDumpSectionDataLines)
    {
        // Decode section addresses and if in range, save the data as data records
        int startIndex = 1;
        int eolIndex = 0;
        int asciiPrintIndex;
        int dataSeperatorIndex;
        int wordByteCount;
        int dataCount;
        int bufUsed = 0;
        byte[] buf = new byte[elfDumpSectionDataLines.length() / 2];

        this.address = -1;

        while(eolIndex >= 0)
        {
            dataSeperatorIndex = elfDumpSectionDataLines.indexOf(' ', startIndex + 1);  // +1 is for unneeded starting space on each line
            if(dataSeperatorIndex < 0)
            {
                break;
            }
            asciiPrintIndex = elfDumpSectionDataLines.indexOf("  ", dataSeperatorIndex + 1);  // +1 is for size of the data seperator space
            if(asciiPrintIndex < 0)
            {
                break;
            }
            wordByteCount = ((elfDumpSectionDataLines.indexOf(' ', dataSeperatorIndex + 1) - dataSeperatorIndex) / 2);
            eolIndex = elfDumpSectionDataLines.indexOf('\n', asciiPrintIndex + 2);
            if(this.address < 0)
            {
                this.architecture16Bit = wordByteCount == 3;

                try
                {
                    this.address = Long.decode("0x" + elfDumpSectionDataLines.substring(startIndex, dataSeperatorIndex));
                }
                catch(NumberFormatException ex) // Happens if we've exited the Section data record and the line provided isn't another part of the last section
                {
                    return;
                }
            }
            startIndex = eolIndex + 2;  // +2 is for unneeded \n and space characters

            // Decode the data on this line and add it to data record
            String encodedData = elfDumpSectionDataLines.substring(dataSeperatorIndex + 1, asciiPrintIndex);
            encodedData = encodedData.replaceAll(" ", "");
            dataCount = encodedData.length() / 2;
            if(dataCount > buf.length - bufUsed)
            {
                buf = Arrays.copyOf(buf, bufUsed + dataCount + 1024);
            }
            for(int i = 0; i < dataCount; i++)
            {
                buf[bufUsed + i] = (byte)(Integer.decode("0x" + encodedData.substring(i * 2, i * 2 + 2)) & 0xFF);
            }
            bufUsed += dataCount;
        }

        this.data = Arrays.copyOf(buf, bufUsed);    // Truncate array to correct size
    }

    // Checks if the the dataRecord.data contents match, among architecture and 
    // address, not necessarily that they are a both references to the same 
    // array or that dataRecord is a reference to this class.
    public boolean equals(DataRecord dataRecord)
    {
        if((dataRecord.architecture16Bit != this.architecture16Bit) || (dataRecord.address != this.address))
            return false;
        if((dataRecord.data == null) && (this.data == null))
            return true;
        else if(dataRecord.data == null)
            return false;
        else if(this.data == null)
            return false;
        if(dataRecord.data.length != this.data.length)
            return false;

        return Arrays.equals(dataRecord.data, this.data);
    }

    @Override
    public int compareTo(DataRecord y)
    {
        return this.address == y.address ? 0 : ((int)this.address > (int)y.address ? 1 : -1);
    }

    @Override
    public int compare(DataRecord x, DataRecord y)
    {
        return x.address == y.address ? 0 : ((int)x.address > (int)y.address ? 1 : -1);
    }

    @Override
    public String toString()
    {
        List<String> retStrings = new ArrayList<String>();
        retStrings.add(String.format("16-bit: %s, Address: 0x%" + (architecture16Bit ? "06X" : "08X"), architecture16Bit, address));
        retStrings.add(String.format("assignedMemory: %s", assignedMemory));
        retStrings.add(String.format("comment: %s", comment));
        if(data != null)
        {
            retStrings.add(String.format("data: %d bytes", data.length));
            for(int i = 0; i < data.length - 8; i += 8)
            {
                retStrings.add(String.format("%02X%02X%02X%02X %02X%02X%02X%02X", data[i + 0], data[i + 1], data[i + 2], data[i + 3], data[i + 4], data[i + 5], data[i + 6], data[i + 7]));
            }
            String residual = "";
            for(int i = data.length - (data.length % 8); i < data.length; i++)
            {
                residual += String.format("%02X", data[i]);
                if((i + 1) % 4 == 0)
                    residual += " ";
            }
            if(!residual.isEmpty())
                retStrings.add(residual);
        }

        return CatStringList(retStrings, "\n");
    }

    public void normalizePIC32Addresses()
    {
        if(this.architecture16Bit)
            return;

        if((this.address & 0xFFC00000L) == 0xA0000000L)    // Make kseg1_data_mem addresses kseg0_data_mem addresses
        {
            this.address ^= 0x20000000L;
        }
        if((this.address & 0xFFC00000L) == 0x1D000000L)    // Make main flash physical addresses kseg0_flash_mem addresses
        {
            this.address ^= 0x80000000L;
        }
        if((this.address & 0xFFC00000L) == 0x1FC00000L)    // Make boot flash/Config word physical addresses kseg1_flash_mem addresses
        {
            this.address ^= 0xA0000000L;
        }
    }

    /**
     * Create a new MemoryRegion to represent the section addresses and attempts
     * to locate the proper device memory region from the list that this section
     * lives in. If found, additional metadata from the reference deviceRegion
     * is copied to retain information like the erase and programming alignment.
     *
     * @param deviceRegions Reference region list for the various memory types
     *                      on the target device.
     *
     * @return A new MemoryRegion, containing start and end addresses to match
     *         the section load memory address range and updated with metadata
     *         from the matching deviceRegion, if found. If no match is found,
     *         the MemoryRegion will have less meta data set in it derived soley
     *         from the section attributes and properties.
     */
    public MemoryRegion mapToDeviceRegion(List<MemoryRegion> deviceRegions, Partition expectedPartition)
    {
        MemoryRegion ret = new MemoryRegion(this.address, this.getEndAddress());
        ret.type = MemType.ROM;
        if(this.data != null)
        {
            ret.name = this.assignedMemory;
            ret.endAddr = this.getEndAddress();
        }
        if(expectedPartition == null)
            expectedPartition = Partition.single;
        ret.partition = expectedPartition;

        if((ret.startAddr & 0xFFC00000L) == 0xA0000000L)    // Make kseg1_data_mem addresses kseg0_data_mem addresses
        {
            ret.startAddr ^= 0x20000000L;
            ret.endAddr ^= 0x20000000L;
        }
        if((ret.startAddr & 0xFFC00000L) == 0x1D000000L)    // Make main flash physical addresses kseg0_flash_mem addresses
        {
            ret.startAddr |= 0x80000000L;
            ret.endAddr |= 0x80000000L;
        }
        if(((ret.startAddr & 0x7FC00000L) == 0x1FC00000L) // Make boot flash/Config word physical or kseg0 addresses kseg1 uncached addresses
           || ((ret.startAddr & 0x7FC00000L) == 0x1F800000L)) // Make SFR physical or kseg0 addresses kseg1 uncached addresses
        {
            ret.startAddr |= 0xA0000000L;
            ret.endAddr |= 0xA0000000L;
        }

        MemoryRegion.Partition p = expectedPartition;
        while(true)
        {
            for(MemoryRegion mr : deviceRegions)
            {
                if((ret.startAddr >= mr.endAddr) || (ret.endAddr <= mr.startAddr) || (mr.partition != p))   // Skip if no overlap at all
                    continue;

                if(mr.isProgSpace())
                {
                    ret.copyMetaData(mr);
                    if((this.data != null) && (this.getEndAddress() > mr.endAddr)) // Flag truncated section end address if it spills into a different device memory region
                        ret.endAddr = mr.endAddr;
                    if(ret.startAddr < mr.startAddr)    // Keep looking if we a start outside a known range but end within a range, now truncated to the end of the range
                        continue;
                    return ret;
                }
            }

            // Didn't find a good match for start address and type, so look again and match against the closest deviceRegion that preceeds the section
            for(MemoryRegion mr : deviceRegions)
            {
                if(mr.partition != p)
                    continue;

                if(mr.isProgSpace())
                {
                    if((ret.endAddr > mr.startAddr) && (ret.startAddr < mr.endAddr))
                    {   // Device region found fully contained within the section data, so flag section for end address truncation
                        ret.endAddr = mr.startAddr;
                    }
                    if(ret.startAddr == mr.endAddr)
                    {
                        ret.copyMetaData(mr);
                        ret.comment = "no-device-mem-defined";
                    }
                }
            }

            if(p == MemoryRegion.Partition.single)
                break;
            p = MemoryRegion.Partition.single;   // Try searching again using partition 1 as the critera since some things are global and not in the partition 1/partition 2 view
        }
        return ret;
    }

    /**
     * Prints the data byte array as .pword/.word and .pbyte/.byte ASM
     * directives for inclusion in an asm file.
     *
     * @return String formatted and representing all data in the record.
     */
    public String getDataAsASMCode(List<Symbol> symExports)
    {
        if((this.data == null) || (this.data.length == 0))
            return "";

        List<String> ret = new ArrayList<>();
        long dAddr = this.address;
        char charForm[];
        String residual;

        int i;
        int j;
        charForm = new char[3 * 8];

        // Print the code and constants in this range
        if(this.architecture16Bit)  // XC16
        {
            for(i = 0; i <= this.data.length - (3 * 8); i += 3 * 8)
            {
                for(int k = 0; k < 3 * 8; k++)
                {
                    charForm[k] = ((this.data[i + k] >= ' ') && (this.data[i + k] <= '~')) ? (char)this.data[i + k] : '.';
                }
                int instWords[] = BytesToInt24s(data, i, i + 24);
                ret.add(String.format("    .pword      0x%06X, 0x%06X, 0x%06X, 0x%06X, 0x%06X, 0x%06X, 0x%06X, 0x%06X  /* 0x%06X %s */",
                                      instWords[0], instWords[1], instWords[2], instWords[3], instWords[4], instWords[5], instWords[6], instWords[7],
                                      dAddr, new String(charForm)));
                dAddr += 0x10;
            }
            residual = "    .pword      ";
            for(j = 0; i <= this.data.length - 3; i += 3)
            {
                for(int k = 0; k < 3; k++)
                {
                    charForm[j++] = ((this.data[i + k] >= ' ') && (this.data[i + k] <= '~')) ? (char)this.data[i + k] : '.';
                }
                residual += String.format("0x%06X, ", BytesToInt24s(data, i, i + 3)[0]);
            }
            if(j != 0)
            {
                ret.add(String.format("%-95s /* 0x%06X %-24s */", residual.subSequence(0, residual.length() - 2), dAddr, new String(charForm).substring(0, j)));
                dAddr += j / 3 * 2;
            }

            residual = "    .pbyte      ";
            for(j = 0; i < this.data.length; i++)
            {
                charForm[j++] = ((this.data[i] >= ' ') && (this.data[i] <= '~')) ? (char)this.data[i] : '.';
                residual += String.format("0x%02X, ", this.data[i]);
            }
            if(j != 0)
                ret.add(String.format("%-95s  /* 0x%06X %-24s */", residual.subSequence(0, residual.length() - 2), dAddr, new String(charForm).substring(0, j)));
        }
        else    // XC32
        {
            long endAddr = this.getEndAddress();
            i = 0;
            long lastSymAddr = dAddr - 1;
            long lastSymSize = 0;
            List<Symbol> lastSymsOpened = new ArrayList<>();
            Symbol lastSymEnt = null;
            TreeMap<Long, List<Symbol>> sortedSyms = new TreeMap<>();
            Long symStopAddr = null;

            if((symExports != null) && !symExports.isEmpty())
            {
                for(Symbol s : symExports)
                {
                    if(sortedSyms.containsKey(s.address))
                    {
                        sortedSyms.get(s.address).add(s);
                    }
                    else
                    {
                        List<Symbol> symsAtThisAddr = new ArrayList<>();
                        symsAtThisAddr.add(s);
                        sortedSyms.put(s.address, symsAtThisAddr);
                    }
                }
            }

            while(dAddr < endAddr)
            {
                if((symExports != null) && !symExports.isEmpty())
                    symStopAddr = sortedSyms.higherKey(lastSymAddr);
                if(symStopAddr == null)
                    symStopAddr = endAddr;
                if(((dAddr & 0x1L) == 1L) && (dAddr != symStopAddr))
                {
                    ret.add(String.format("    .byte       0x%02X", data[i]));
                    dAddr++;
                    i++;
                }
                if(((dAddr & 0x2L) == 2L) && (dAddr != symStopAddr))
                {
                    ret.add(String.format("    .short      0x%04X", BytesToInt16s(data, i, i + 2)[0]));
                    dAddr += 2;
                    i += 2;
                }
                for(; dAddr <= symStopAddr - 0x04; dAddr += 0x4, i += 4)
                {
                    ret.add(String.format("    .word       0x%08X", BytesToInt32s(data, i, i + 4)[0]));
                }
                for(; dAddr <= symStopAddr - 0x2; dAddr += 0x2, i += 2)
                {
                    ret.add(String.format("    .short      0x%04X", BytesToInt16s(data, i, i + 2)[0]));
                }
                for(; dAddr < symStopAddr; dAddr++, i++)
                {
                    ret.add(String.format("    .byte       0x%02X", data[i]));
                }
                if((sortedSyms != null) && !sortedSyms.isEmpty())
                {
                    List<Symbol> symsAtAddress = sortedSyms.get(symStopAddr);
                    if(symsAtAddress != null)
                    {
                        for(Symbol lastSym : lastSymsOpened)
                        {
                            ret.add(String.format("    .size       %1$s, . - %1$s   # %2$d", lastSym.name, lastSymSize));
                            if((lastSymEnt != null) && lastSymEnt.equals(lastSym))
                                ret.add(String.format("    .end        %s", lastSym.name));
                        }
                        lastSymsOpened.clear();
                        ret.add("");
                        boolean mmNeedsPrinting = false;
                        for(Symbol sym : symsAtAddress)
                        {
                            if(sym.flags.function)
                            {
                                if(!mmNeedsPrinting)
                                {
                                    ret.add(String.format("    .ent        %s", sym.name));
                                    lastSymEnt = sym;
                                }
                                ret.add(String.format("    .type       %s, @function", sym.name));
                                Long symStopAddr2 = sortedSyms.higherKey(symStopAddr);
                                if(symStopAddr2 == null)
                                    symStopAddr2 = endAddr;
                                lastSymSize = symStopAddr2 - symStopAddr;
                                lastSymsOpened.add(0, sym);
                                mmNeedsPrinting = true;
                            }
                            else if(sym.flags.object)
                            {
                                ret.add(String.format("    .type       %s, @object", sym.name));
                            }
                            ret.add(String.format("%-31s # 0x%08X", sym.name + ":", sym.address));
                        }
                        if(mmNeedsPrinting)
                        {
                            ret.add(String.format("    .set        micromips"));
                            ret.add(String.format("    .insn"));
                        }
                    }
                }

                lastSymAddr = symStopAddr;
            }

            for(Symbol lastSym : lastSymsOpened)
            {
                ret.add(String.format("    .size       %1$s, . - %1$s   # %2$d", lastSym.name, lastSymSize));
                if((lastSymEnt != null) && lastSymEnt.equals(lastSym))
                    ret.add(String.format("    .end        %s", lastSym.name));
            }
            lastSymsOpened.clear();
        }

        return CatStringList(ret, "\n");
    }
    /**
     * Splits a data record into two. The original is modified (lower address
     * portion to the left), while the split off portion (on the right) is
     * returned.
     *
     * @param splitAtAddress Address within the record to split
     *
     * @return Data on the right (at higher addresses) that has been split off.
     *         If the split address doesn't exist in the DataRecord, then
     *         nothing changes and null is returned.
     */
    public DataRecord SplitAtAddress(long splitAtAddress)
    {
        DataRecord newRecord = new DataRecord(this.architecture16Bit);
        newRecord.address = splitAtAddress;
        newRecord.assignedMemory = this.assignedMemory;
        newRecord.comment = this.comment;

        // Copy extra data into the new record
        byte newBaseData[] = new byte[this.getDataIndexOfAddress(splitAtAddress)];
        if(newBaseData.length > this.data.length)  // But return null if the address was illegal and doesn't exist in the given record
        {
            return null;
        }
        newRecord.data = new byte[this.data.length - newBaseData.length];
        System.arraycopy(this.data, newBaseData.length, newRecord.data, 0, newRecord.data.length);

        // Shrink the original record data
        System.arraycopy(this.data, 0, newBaseData, 0, newBaseData.length);
        this.data = newBaseData;

        return newRecord;
    }

    public boolean isArchitecture16Bit()
    {
        return architecture16Bit;
    }

    public void setArchitecture16Bit(boolean architecture16Bit)
    {
        this.architecture16Bit = architecture16Bit;
    }

    public long getAddress()
    {
        return address;
    }

    public void setAddress(long address)
    {
        this.address = address;
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }

    public String getAssignedMemory()
    {
        return assignedMemory;
    }

    public void setAssignedMemory(String assignedMemory)
    {
        this.assignedMemory = assignedMemory;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    /**
     * Processes a List of DataRecord classes and splits longer records that
     * exceed maxRecordAddresses into two or more DataRecord elements containing
     * no more than maxRecordAddresses addresses in each record. Each split
     * record is maximally sized, so when a split occurs, the first records are
     * exactly maxRecordAddresses long while the last one in the split can be 0
     * to maxRecordAddresses long.
     *
     * @param records            List of DataRecord classes to process.
     * @param maxRecordAddresses Number of addresses that every output record
     *                           should match or be smaller than.
     */
    public static void SplitRecordsByLength(List<DataRecord> records, int maxRecordAddresses)
    {
        for(int i = 0; i < records.size(); i++)
        {
            DataRecord r = records.get(i);
            if(r.getEndAddress() - r.address > maxRecordAddresses)
            {
                records.add(i + 1, r.SplitAtAddress(r.address + maxRecordAddresses));
            }
        }
    }

    /**
     * Sorts a List of DataRecords by record address and combines any data
     * regions which overlap each other such that no data record address range
     * overlap can exist afterwards. Data is combined by doing a bitwise AND
     * operation per overlapping byte.
     *
     * @param records List of DataRecord classes to sort and combine.
     *
     * @return Hashmap containing all data in the input records, copied to
     *         individual byte arrays, combined by bitwise ANDing and keyed in
     *         the map by their starting address. If a block of data contains
     *         less than the size of a full block, one or more 0xFF bytes are
     *         generated such that all data returned in the map is a fixed
     *         single address to exact block size, much as a flash memory
     *         behaves.
     */
    public static HashMap<Long, Byte[]> CombineData(List<DataRecord> records)
    {
        if(records == null)
            return null;

        HashMap<Long, Byte[]> map = new HashMap<Long, Byte[]>();
        for(int i = 0; i < records.size(); i++)
        {
            DataRecord dr = records.get(i);
            if((dr.data == null) || (dr.data.length == 0))
            {
                records.remove(i--);
                continue;
            }
            int combineBlockByteLen = 4;
            int combineBlockAddrLen = 0x4;
            if(dr.architecture16Bit)
            {
                combineBlockByteLen = 3;
                combineBlockAddrLen = 0x2;
            }
            for(long j = dr.address; j < dr.getEndAddress(); j += combineBlockAddrLen)
            {
                Byte[] wordData = new Byte[combineBlockByteLen];
                int index = dr.getDataIndexOfAddress(j);
                for(int k = 0; k < combineBlockByteLen; k++)    // Copy data, generating 0xFF bytes if needed
                {
                    if(index >= dr.data.length)
                    {
                        wordData[k] = (byte)0xFF;
                        continue;
                    }
                    wordData[k] = dr.data[index++];
                }
                if(map.containsKey(j))
                {
                    Byte[] existingData = map.get(j);
                    for(int k = 0; k < existingData.length; k++)
                    {
                        existingData[k] = (byte)(existingData[k] & wordData[k]);
                    }
                    map.put(j, existingData);
                    continue;
                }
                map.put(j, wordData);
            }
        }
        return map;
    }

    /**
     * Sorts a List of DataRecords by record address and coalesces adjacent
     * records that directly abut each other into the minimum number of total
     * records.
     *
     * @param records List of DataRecord classes to sort and coalesce.
     */
    public static void CoalesceRecords(List<DataRecord> records)
    {
        CoalesceRecords(records, true);
    }

    /**
     * Optionally sorts a List of DataRecords by record address and coalesces
     * adjacent records that directly abut each other into the minimum number of
     * total records. Data records with null data, or data.length == 0 are also
     * removed from the records list.
     *
     * @param records List of DataRecord classes to optionally sort and then
     *                coalesce.
     *
     * @param presort true if a record address sort should be performed before
     *                looking for records which are adjacent to each other. This
     *                can often result in better coalescing results.
     *
     * Set to false if no record sorting should be performed and only the
     * coalescing step should occur.
     */
    public static void CoalesceRecords(List<DataRecord> records, boolean presort)
    {
        CoalesceRecords(records, presort, 0, 0);
    }

    public static void CoalesceRecords(List<DataRecord> records, boolean presort, long leftAlign, long rightAlign)
    {
        long testNextAddr;
        DataRecord dr;
        DataRecord dr2;
        int coalesceSizeUsed = 0;
        long leftAlignAddrs, rightAlignAddrs;
        int leftAlignBytesNeeded, rightAlignBytesNeeded;
        int bufLen;

        if((records == null) || (records.size() <= 1))
            return;

        if(leftAlign == 0)
            leftAlign = 1;
        if(rightAlign == 0)
            rightAlign = 1;

        if(presort)
            Collections.sort(records);  // Sort the records by start address to increase likelihood of finding abutting records

        // Iterate over all records
        for(int i = 0; i < records.size() - 1; i++)
        {
            dr = records.get(i);
            if((dr.data == null) || (dr.data.length == 0))
            {
                records.remove(i--);
                continue;
            }
            leftAlignAddrs = dr.address % leftAlign;
            leftAlignBytesNeeded = (int)(dr.architecture16Bit ? leftAlignAddrs / 2 * 3 : leftAlignAddrs);
            if(leftAlignBytesNeeded != 0)
            {
                dr.address -= leftAlignAddrs;
                dr.data = Arrays.copyOfRange(dr.data, leftAlignBytesNeeded, dr.data.length + leftAlignBytesNeeded);
                Arrays.fill(dr.data, 0, leftAlignBytesNeeded, (byte)0xFF);
            }
            if(dr.architecture16Bit && (dr.data.length % 3 != 0))
            {
                int oldLen = dr.data.length;
                dr.data = Arrays.copyOf(dr.data, oldLen + (dr.data.length % 3));
                Arrays.fill(dr.data, oldLen, dr.data.length, (byte)0xFF);
            }
            testNextAddr = dr.address + (dr.architecture16Bit ? (dr.data.length / 3 * 2) : dr.data.length);
            rightAlignAddrs = (testNextAddr % rightAlign == 0) ? 0 : rightAlign - ((testNextAddr + rightAlign) % rightAlign);
            coalesceSizeUsed = dr.data.length;
            bufLen = coalesceSizeUsed;
            rightAlignBytesNeeded = (int)(dr.architecture16Bit ? rightAlignAddrs / 2 * 3 : rightAlignAddrs);
            if(coalesceSizeUsed + rightAlignBytesNeeded > bufLen)
            {
                bufLen = coalesceSizeUsed + rightAlignBytesNeeded + 8192;
                bufLen -= bufLen % 8192;
                dr.data = Arrays.copyOf(dr.data, bufLen);
            }
            for(int j = i + 1; j < records.size(); j++)
            {
                dr2 = records.get(j);
                if((dr.architecture16Bit != dr2.architecture16Bit) || (testNextAddr + rightAlignAddrs < dr2.address - (dr2.address % leftAlign)))
                    break;
                rightAlignAddrs = dr2.address - testNextAddr;
                if(rightAlignAddrs < 0 || (rightAlignAddrs > rightAlign + leftAlign)) // Possible when presort == false
                    break;
                rightAlignBytesNeeded = (int)(dr.architecture16Bit ? rightAlignAddrs / 2 * 3 : rightAlignAddrs);
                if(coalesceSizeUsed + rightAlignBytesNeeded + dr2.data.length > bufLen)
                {
                    bufLen = coalesceSizeUsed + rightAlignBytesNeeded + dr2.data.length + 8192;
                    bufLen -= bufLen % 8192;
                    dr.data = Arrays.copyOf(dr.data, bufLen);
                }
                Arrays.fill(dr.data, coalesceSizeUsed, coalesceSizeUsed + rightAlignBytesNeeded, (byte)0xFF);
                coalesceSizeUsed += rightAlignBytesNeeded;
                System.arraycopy(dr2.data, 0, dr.data, coalesceSizeUsed, dr2.data.length);
                coalesceSizeUsed += dr2.data.length;
                records.remove(j--);
                testNextAddr = dr.address + (dr.architecture16Bit ? (coalesceSizeUsed + 2) / 3 * 2 : coalesceSizeUsed);
                rightAlignAddrs = (testNextAddr % rightAlign == 0) ? 0 : rightAlign - ((testNextAddr + rightAlign) % rightAlign);
            }
            rightAlignBytesNeeded = (int)(dr.architecture16Bit ? rightAlignAddrs / 2 * 3 : rightAlignAddrs);
            if(rightAlignBytesNeeded > 0)
            {
                Arrays.fill(dr.data, coalesceSizeUsed, coalesceSizeUsed + rightAlignBytesNeeded, (byte)0xFF);
                coalesceSizeUsed += rightAlignBytesNeeded;
            }

            if(bufLen != coalesceSizeUsed)
                dr.data = Arrays.copyOf(dr.data, coalesceSizeUsed);
        }
    }

    /**
     * Searches a List of DataRecords for the given elements in the specified
     * address range and trims them out of the list, returning trimmed data as a
     * new DataRecord.
     *
     * @param recordsToExtractFrom List of DataRecords to search for the
     *                             extraction addresses
     * @param extractStartAddress  Start address, inclusive, of data we wish to
     *                             remove/return
     * @param extractEndAddress    End address, exclusive, of the data we wish
     *                             to remove/return
     *
     * @return <p>
     * A new DataRecord containing the extracted address range. If there are
     * multiple records containing chunks of the extract range, only the first
     * fragment found is returned (fragment subject to coalescing). In such
     * cases, all chunks are still removed from the recordsToExtractFrom list.
     * See ExtractRanges() for a function that returns all fragments.</p>
     *
     * <p>
     * If no part of the address range exists in the given recordsToExtractFrom,
     * or if recordsToExtractFrom is null, then null is returned.</p>
     */
    public static DataRecord ExtractRange(List<DataRecord> recordsToExtractFrom, long extractStartAddress, long extractEndAddress)
    {
        List<DataRecord> retRecords = ExtractRanges(recordsToExtractFrom, extractStartAddress, extractEndAddress);

        if((retRecords == null) || retRecords.isEmpty())
        {
            return null;
        }
        if(retRecords.size() > 1)
        {
            Collections.sort(retRecords);
            // TODO: Should have an assert or something if we have records that only overlap with 0xFFFFFF padding data
        }
        return retRecords.get(0);
    }

    public static DataRecord ExtractRangeFromSections(List<Section> sectionsToExtractFrom, long extractStartAddress, long extractEndAddress)
    {
        if(sectionsToExtractFrom == null)
            return null;

        List<DataRecord> sectionData = new ArrayList<DataRecord>();

        for(Section sec : sectionsToExtractFrom)
        {
            if(sec.data != null)
            {
                sectionData.add(sec.data);
            }
        }

        return DataRecord.ExtractRange(sectionData, extractStartAddress, extractEndAddress);
    }

    /**
     * Searches a List of DataRecords for data within the specified address
     * range and trims them out of the list, returning trimmed data records as a
     * new list of DataRecords. If a data record contains data that straddles
     * the start or end address, it is cloned into a new DataRecord, the data
     * arrays for both DataRecords are trimmed to exactly hit the address range,
     * and the new cloned one is returned. If a data record straddles both start
     * and end addresses, the left part (lowest address range) is retained in
     * recordsToExtractFrom, the middle part cloned and returned, and the right
     * part cloned and inserted into recordsToExtractFrom.
     *
     * @param recordsToExtractFrom List of DataRecords to search for the
     *                             extraction addresses
     * @param extractStartAddress  Start address, inclusive, of data we wish to
     *                             remove/return
     * @param extractEndAddress    End address, exclusive, of the data we wish
     *                             to remove/return
     *
     * @return <p>
     * A list of DataRecords containing the extracted address range.</p>
     *
     * <p>
     * If no part of the address range exists in the given recordsToExtractFrom,
     * an empty list is returned.</p>
     *
     * <p>
     * If recordsToExtractFrom is null, null is returned.</p>
     */
    public static List<DataRecord> ExtractRanges(List<DataRecord> recordsToExtractFrom, long extractStartAddress, long extractEndAddress)
    {
        List<DataRecord> retRecords = new ArrayList<>();

        if(recordsToExtractFrom == null)
        {
            return null;
        }

        for(int i = 0; i < recordsToExtractFrom.size(); i++)
        {
            DataRecord record = recordsToExtractFrom.get(i);
            long recEnd = record.getEndAddress();

            if((record.address < extractEndAddress) && (recEnd > extractStartAddress))
            {
                // Overlap exists, determine how to handle it
                if((record.address >= extractStartAddress) && (recEnd <= extractEndAddress))
                {// Full record enclosed case, just move the record without changing it
                    recordsToExtractFrom.remove(record);
                    retRecords.add(record);
                    i--;
                    continue;
                }
                else if((record.address <= extractStartAddress) && (recEnd >= extractEndAddress))
                {// Full extract range enclosed case, extract middle of record with left and right portions remaining
                    int leftBytes = record.architecture16Bit ? (int)((extractStartAddress - record.address) * 3 / 2) : (int)(extractStartAddress - record.address);
                    int rightBytes = record.architecture16Bit ? (int)((recEnd - extractEndAddress) * 3 / 2) : (int)(recEnd - extractEndAddress);
                    int midBytes = record.data.length - leftBytes - rightBytes;
                    DataRecord right = record.Clone();
                    DataRecord mid = record.Clone();
                    mid.address = extractStartAddress;
                    mid.data = Arrays.copyOfRange(mid.data, leftBytes, leftBytes + midBytes);
                    if(rightBytes > 0)
                    {
                        right.address = extractEndAddress;
                        right.data = Arrays.copyOfRange(right.data, leftBytes + midBytes, leftBytes + midBytes + rightBytes);
                        recordsToExtractFrom.add(right);
                        i++;        // Increment i since we left the left side behind and also added one that can't be overlapping
                    }
                    if(leftBytes > 0)
                    {
                        record.data = Arrays.copyOf(record.data, leftBytes);    // Shrink record bytes to create left side to remain in place within recordsToExtractFrom
                    }
                    else
                    {
                        recordsToExtractFrom.remove(record);
                        i--;
                    }
                    retRecords.add(mid);
                    continue;
                }
                else if(record.address < extractStartAddress)
                {   // Keep left side, return right side
                    int leftBytes = record.architecture16Bit ? (int)((extractStartAddress - record.address) * 3 / 2) : (int)(extractStartAddress - record.address);
                    int rightBytes = record.data.length - leftBytes;
                    DataRecord right = record.Clone();
                    record.data = Arrays.copyOf(record.data, leftBytes);
                    right.address = extractStartAddress;
                    right.data = Arrays.copyOfRange(right.data, leftBytes, leftBytes + rightBytes);
                    retRecords.add(right);
                    continue;
                }
                else
                {// Return left side, keep right side
                    int rightBytes = record.architecture16Bit ? (int)((recEnd - extractEndAddress) * 3 / 2) : (int)(recEnd - extractEndAddress);
                    int leftBytes = record.data.length - rightBytes;
                    DataRecord left = record.Clone();
                    record.data = Arrays.copyOfRange(record.data, leftBytes, leftBytes + rightBytes);
                    left.address = extractEndAddress;
                    left.data = Arrays.copyOfRange(left.data, 0, leftBytes);
                    retRecords.add(left);
                    continue;
                }
            }
        }

        DataRecord.CoalesceRecords(retRecords);
        return retRecords;
    }

    /**
     * Returns a 24-bit value located at the specified address (32-bit value on
     * non-16bit devices). If the address does not lie within the given
     * DataRecord, 0xFFFFFF (0xFFFFFFFF) is returned instead.
     *
     * If a full block of 24-bit/32-bits of data does not exist at the specified
     * address, 0xFF padding bytes are generated (on the big-end of the word).
     *
     * @param address Address to read from
     */
    public int GetIntDataAtAddress(long address)
    {
        int index = this.getDataIndexOfAddress(address);

        if(architecture16Bit)
        {
            if((index < 0) || (index + 3 > this.data.length))
            {
                if(index + 2 == this.data.length)
                    return (int)(this.data[index] & 0xFF) | (((int)(this.data[index + 1] & 0xFF)) << 8) | (0xFF << 16);
                if(index + 1 == this.data.length)
                    return (int)(this.data[index] & 0xFF) | (0x00FFFF << 8);
                return 0xFFFFFF;
            }
            return (int)(this.data[index] & 0xFF) | (((int)(this.data[index + 1] & 0xFF)) << 8) | (((int)(this.data[index + 2] & 0xFF)) << 16);
        }

        if((index < 0) || (index + 4 > this.data.length))
        {
            if(index + 3 == this.data.length)
                return (int)(this.data[index] & 0xFF) | (((int)(this.data[index + 1] & 0xFF)) << 8) | (((int)(this.data[index + 2] & 0xFF)) << 16) | (0xFF << 24);
            if(index + 2 == this.data.length)
                return (int)(this.data[index] & 0xFF) | (((int)(this.data[index + 1] & 0xFF)) << 8) | (0xFFFF << 16);
            if(index + 1 == this.data.length)
                return (int)(this.data[index] & 0xFF) | (0xFFFFFF << 8);
            return 0xFFFFFFFF;
        }
        return (int)(this.data[index] & 0xFF) | (((int)(this.data[index + 1] & 0xFF)) << 8) | (((int)(this.data[index + 2] & 0xFF)) << 16) | (((int)(this.data[index + 3] & 0xFF)) << 24);
    }

    /**
     * Returns a 16-bit value located at the specified address. If the address
     * does not lie within the given DataRecord, 0xFFFF is returned instead.
     *
     * @param address Address to read from
     */
    public int GetShortDataAtAddress(long address)
    {
        int index = this.getDataIndexOfAddress(address);

        if((index < 0) || (index > this.data.length - 2))
        {
            return 0xFFFF;
        }
        return (int)(this.data[index] & 0xFF) | (((int)(this.data[index + 1] & 0xFF)) << 8);
    }

    /**
     * Returns the memory address just after the last byte of data in the
     * record. On 16-bit devices, this will always be a legal (even program
     * memory address), even if the data only occupies 1 or 2 bytes of the
     * instruction word.
     */
    public long getEndAddress()
    {
        long ret = address + (architecture16Bit ? Blob.BytesToMCU16Addresses(data.length) : data.length);
        if((address & 0xFFFF0000L) == 0xFFFF0000L)    // Trucate to 32-bits if we overflowed due to a negative starting address
        {
            ret &= 0xFFFFFFFFL;
        }
        return ret;
    }

    public int getDataIndexOfAddress(long address)
    {
        return architecture16Bit ? (int)((address - this.address) / 2 * 3) : (int)(address - this.address);
    }

    public long getAddressOfDataIndex(int dataIndex)
    {
        return address + (architecture16Bit ? ((dataIndex + 2) / 3 * 2) : dataIndex);
    }

    public AddressRange getAddressRange()
    {
        return new AddressRange(this.address, this.getEndAddress());
    }

    /**
     * Returns all used addresses as a List of AddressRanges by the given list
     * of DataRecords.
     *
     * @param dataRecords List of DataRecords to determine the addresses from.
     *                    The architecture16Bit field is observed when trying to
     *                    determine the correct size to address mapping.
     *
     * @return List of AddressRange's corresponding to the given DataRecords.
     */
    static public List<AddressRange> convertToAddressRanges(List<DataRecord> dataRecords)
    {
        List<AddressRange> ret = new ArrayList<AddressRange>();
        for(DataRecord dr : dataRecords)
        {
            ret.add(new AddressRange(dr.address, dr.getEndAddress()));
        }
        return ret;

    }

    public MemoryRegion getMemoryRegion()
    {
        MemoryRegion ret = new MemoryRegion(this.address, this.address);
        if(this.data != null)
        {
            ret.endAddr += this.architecture16Bit ? (this.data.length + 2) / 3 * 0x2 : this.data.length;
        }
        ret.type = MemType.ROM;
        if(this.assignedMemory.equals("data"))
            ret.type = MemType.RAM;
        ret.comment = this.comment;
        return ret;
    }
}
