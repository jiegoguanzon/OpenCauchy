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

import java.io.*;
import java.util.*;


public class AddressRange implements Serializable, Cloneable, Comparable<AddressRange>
{
    static final long serialVersionUID = 1L;
    public long startAddr;   // Inclusive
    public long endAddr;     // Exclusive (points to 'one' higher than the last element within the address range; 'one' may be 0x2 or some other value if the underlying architecture is intrinsically aligned to some minimum address increment)

    public AddressRange()
    {
        startAddr = 0;
        endAddr = 0;
    }

    public AddressRange(MemoryRegion mr)
    {
        startAddr = mr.getStartAddr();
        endAddr = mr.getEndAddr();
    }

    public AddressRange(long startAddr_inclusive, long endAddress_exclusive)
    {
        startAddr = startAddr_inclusive;
        endAddr = endAddress_exclusive;
    }

    public AddressRange(long startAddr_inclusive, int length)
    {
        startAddr = startAddr_inclusive;
        endAddr = startAddr + length;
    }

    public AddressRange(String startAndEndAddr)    // Format should be "[xxx=]0x00000000,0x12345678", where xxx are any number of don't care characters, 0x00000000 is the range start address, inclusive, and 0x12345678 is the range end address, exclusive.
    {
        String addresses[];

        // Trim outter quotes if string is passed in as: "-ignore=0x123,0xABC"
        startAndEndAddr = Multifunction.TrimQuotes(startAndEndAddr);

        // Trim everything up to and including an equal sign, if there is one
        if(startAndEndAddr.contains("="))
        {
            startAndEndAddr = startAndEndAddr.replaceFirst(".*[=]", "");
        }

        // Trim quotes again in case if it was passed in as: -ignore="0x123,0xABC"
        startAndEndAddr = Multifunction.TrimQuotes(startAndEndAddr);

        // Trim brackets
        startAndEndAddr = startAndEndAddr.replace("[", "");
        startAndEndAddr = startAndEndAddr.replace("(", "");
        startAndEndAddr = startAndEndAddr.replace("{", "");
        startAndEndAddr = startAndEndAddr.replace("]", "");
        startAndEndAddr = startAndEndAddr.replace(")", "");
        startAndEndAddr = startAndEndAddr.replace("}", "");

        // Remove leading and trailing whitespace
        startAndEndAddr = startAndEndAddr.trim();

        // Split addresses on comma or whitespace
        addresses = startAndEndAddr.split("(\\s*,\\s*)|,|(\\s+)", 2);
        startAddr = Long.decode(addresses[0].trim());
        endAddr = Long.decode(addresses[1].trim());
    }

    @Override
    public int compareTo(AddressRange y)
    {
        return this.startAddr == y.startAddr ? 0 : (this.startAddr > y.startAddr ? 1 : -1);
    }

    @Override
    public AddressRange clone()
    {
        try
        {
            return (AddressRange)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            return new AddressRange(this.startAddr, this.endAddr);
        }
    }

    @Override
    public String toString()
    {
        return String.format("[0x%08X, 0x%08X)", this.startAddr, this.endAddr);
    }

    public MemoryRegion toMemoryRegion()
    {
        return new MemoryRegion(this);
    }

    public AddressRange PadAlign(int alignAddrSize)
    {
        if(alignAddrSize == 0)
            return this;
        startAddr -= startAddr % alignAddrSize;
        endAddr += (endAddr % alignAddrSize != 0) ? alignAddrSize - endAddr % alignAddrSize : 0;
        return this;
    }

    public static void PadAlign(AddressRange ar, int alignAddrSize)
    {
        ar.startAddr -= ar.startAddr % alignAddrSize;
        ar.endAddr += (ar.endAddr % alignAddrSize != 0) ? alignAddrSize - ar.endAddr % alignAddrSize : 0;
    }

    public static List<AddressRange> PadAlign(Collection<AddressRange> addrRanges, int alignAddrSize)
    {
        for(AddressRange ar : addrRanges)
        {
            AddressRange.PadAlign(ar, alignAddrSize);
        }
        return (List<AddressRange>)addrRanges;
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

    /**
     * Sorts the list of address ranges and then reduces the list to the Union
     * of all elements. Address ranges that overlap another address range (or
     * perfectly abut each other) are combined into a single address range.
     * Address Ranges of zero length are removed.
     */
    static void SortAndReduce(List<? extends AddressRange> list)
    {
        Collections.sort(list);
        for(int i = 0; i < list.size() - 1; i++)
        {
            AddressRange ar = list.get(i);
            if(ar.startAddr == ar.endAddr)
            {
                list.remove(i--);
                continue;
            }
            AddressRange ar2 = list.get(i + 1);
            if((ar2.startAddr >= ar.startAddr) && (ar2.startAddr <= ar.endAddr))    // Directly abut or overlap each other
            {
                if(ar2.endAddr > ar.endAddr)
                {
                    ar.endAddr = ar2.endAddr;
                }
                list.remove(i + 1);
                i--;
                continue;
            }
            if(ar2.startAddr == ar2.endAddr)
                list.remove(ar2);
        }
    }

    /**
     * SubtractRange
     *
     * Removes an address range from a list of address ranges, should the given
     * range exist in the list. If the subtraction range lies inside an
     * overlapping range, the non overlapping regions are split into two ranges
     * with the subtraction range removed.
     *
     * @param rangeList     List of AddressRange elements to be subtracted from.
     *                      This list is modified.
     * @param subtractRange An AddressRange element that should be compared for
     *                      overlap against the AddressRanges in the rangeList,
     *                      and in the event there is overlap the rangeList will
     *                      be shrunken or deleted so that no overlap exists.
     */
    static void SubtractRange(List<AddressRange> rangeList, AddressRange subtractRange)
    {
        for(int i = 0; i < rangeList.size(); i++)
        {
            AddressRange ar = rangeList.get(i);
            if((subtractRange.endAddr > ar.startAddr) && (subtractRange.startAddr < ar.endAddr))    // Overlap exists
            {
                if((subtractRange.startAddr >= ar.startAddr) && (subtractRange.endAddr < ar.endAddr))
                {   // Lies in the middle -> must split reference range into two ranges
                    rangeList.add(i + 1, new AddressRange(subtractRange.endAddr, ar.endAddr));
                    ar.endAddr = subtractRange.startAddr;
                }
                else if((subtractRange.startAddr >= ar.startAddr) && (subtractRange.endAddr >= ar.endAddr))
                {   // Overlaps with right half -> trim the right edge off the reference range
                    ar.endAddr = subtractRange.startAddr;
                }
                else if((subtractRange.startAddr <= ar.startAddr) && (subtractRange.endAddr > ar.startAddr))
                {   // Overlaps with left half -> trim the left edge off the reference range
                    ar.startAddr = subtractRange.endAddr;
                }
            }
        }
    }

    /**
     * SubtractRanges
     *
     * Removes all overlapping address ranges from a set of reference ranges.
     *
     * @param referenceRangeList List of AddressRange elements to be subtracted
     *                           from.
     * @param subtractRangeList  List of AddressRange elements that should be
     *                           compared for overlap against the
     *                           referenceRangeList, and in the event there is
     *                           overlap the reference range will be shrunken or
     *                           deleted so that no overlap exists. This does
     *                           not actually modify the referenceRangeList
     *                           elements, but rather makes a copy of it before
     *                           starting any subtraction.
     *
     * @return List of AddressRange elements from the reference list, but now
     *         trimmed and modified by the subtracted ranges. i.e.: return List
     *         = referenceRangeList - subtractRangeList;
     */
    static List<AddressRange> SubtractRanges(List<AddressRange> referenceRangeList, List<AddressRange> subtractRangeList)
    {
        List<AddressRange> ret = new ArrayList<AddressRange>();

        // Make a copy of all of the reference range list elements so we can
        // modify them without disturbing the original
        for(int i = 0; i < referenceRangeList.size(); i++)
        {
            ret.add(new AddressRange(referenceRangeList.get(i).startAddr, referenceRangeList.get(i).endAddr));
        }

        Collections.sort(ret);
        for(int i = 0; i < ret.size(); i++)
        {
            AddressRange ar = ret.get(i);
            for(int j = 0; j < subtractRangeList.size(); j++)
            {
                AddressRange ar2 = subtractRangeList.get(j);
                if((ar2.endAddr > ar.startAddr) && (ar2.startAddr < ar.endAddr))    // Overlap exists
                {
                    if((ar2.startAddr >= ar.startAddr) && (ar2.endAddr < ar.endAddr))
                    {   // Lies in the middle -> must split reference range into two ranges
                        ret.add(i + 1, new AddressRange(ar2.endAddr, ar.endAddr));
                        ar.endAddr = ar2.startAddr;
                    }
                    else if((ar2.startAddr >= ar.startAddr) && (ar2.endAddr >= ar.endAddr))
                    {   // Overlaps with right half -> trim the right edge off the reference range
                        ar.endAddr = ar2.startAddr;
                    }
                    else if((ar2.startAddr <= ar.startAddr) && (ar2.endAddr > ar.startAddr))
                    {   // Overlaps with left half -> trim the left edge off the reference range
                        ar.startAddr = ar2.endAddr;
                    }
                }
            }
        }

        // All subtracted off, now simplify and return
        SortAndReduce(ret);
        return ret;
    }

    /**
     * Subtracts a list of AddressRanges from the addresses occupied by a given
     * list of Sections. Returns the union of address ranges that were common to
     * both lists.
     *
     * @param addressRangeList List of AddressRange elements to be subtracted
     *                         from.
     * @param sectionList      List of Section elements that should be compared
     *                         for overlap against the addressRangeList, and in
     *                         the event there is overlap the reference range
     *                         will be shrunken or deleted so that no overlap
     *                         exists. This does not actually modify the
     *                         addressRangeList elements, but rather makes a
     *                         copy of it before starting any subtraction.
     *
     * @return List of AddressRange elements from the reference list, but now
     *         trimmed and modified by the subtracted ranges. i.e.: return List
     *         = addressRangeList - sectionList;
     */
    static List<AddressRange> SectionAddressUnion(List<AddressRange> addressRangeList, List<Section> sectionList)
    {
        List<AddressRange> sectionRanges = new ArrayList<AddressRange>();

        for(Section sec : sectionList)
        {
            sectionRanges.add(new AddressRange(sec.loadMemoryAddress, sec.size));
        }

        return SubtractRanges(addressRangeList, sectionRanges);
    }
}
