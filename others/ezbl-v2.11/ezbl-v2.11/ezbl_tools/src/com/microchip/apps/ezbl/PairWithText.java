/*
 *******************************************************************************
 * Copyright (c) 2016 Microchip Technology Inc. All rights reserved.
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
import java.util.Comparator;


public class PairWithText implements Serializable, Cloneable, Comparable<PairWithText>
{
    public long first = 0;
    public long second = 0;
    public String text = null;

    public PairWithText(AddressRange ar)
    {
        this.first = ar.startAddr;
        this.second = ar.endAddr;
    }

    public PairWithText(MemoryRegion mr)
    {
        this.first = mr.getStartAddr();
        this.second = mr.getEndAddr();
        this.text = mr.name;
    }

    public PairWithText(AddressRange ar, String text)
    {
        this.first = ar.startAddr;
        this.second = ar.endAddr;
        this.text = text;
    }

    public PairWithText(long first, long second, String text)
    {
        this.first = first;
        this.second = second;
        this.text = text;
    }

    public PairWithText(long first, String text)
    {
        this.first = first;
        this.text = text;
    }

    public PairWithText(long first, long second)
    {
        this.first = first;
        this.second = second;
    }

    public PairWithText(String text)
    {
        this.text = text;
    }

    public PairWithText()
    {
    }

    /**
     * Returns the PairWithText class members in the form: [first hex, second
     * hex] "text"
     *
     * @return A string, for example: "[0x00000000, 0x12345678] "text""
     */
    @Override
    public String toString()
    {
        return String.format("[0x%08X, 0x%08X] \"%s\"", this.first, this.second, this.text);
    }

    public long getFirst()
    {
        return first;
    }

    public void setFirst(long first)
    {
        this.first = first;
    }

    public long getSecond()
    {
        return second;
    }

    public void setSecond(long second)
    {
        this.second = second;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * Compares the 'first' element only as a signed numerical value.
     *
     * @param y PairWithText to compare against
     *
     * @return 0 if both .first elements are equal, -1 if y.first is less than
     *         this' first, or +1 otherwise.
     */
    @Override
    public int compareTo(PairWithText y) // Needed for calling Collections.sort()
    {
        return this.first < y.first ? -1 : this.first == y.first ? 0 : 1;
    }

    /**
     * Compares the 'first' element with the startAddr (only) as a signed
     * numerical value.
     *
     * @param y AddressRange to compare against
     *
     * @return 0 if both .first and .startAddr elements are equal, -1 if
     *         y.startAddr is less than this's .first, or +1 otherwise.
     */
    public int compareTo(AddressRange y)
    {
        return this.first < y.startAddr ? -1 : this.first == y.startAddr ? 0 : 1;
    }


    public static class PIC32MMBootFirstAddrComparator implements Comparator<PairWithText>
    {
        @Override
        public int compare(PairWithText x, PairWithText y)
        {
            // Convert to physical addresses
            long xStart = x.first & 0x1FFFFFFF;
            long yStart = y.first & 0x1FFFFFFF;

            // Same memory type?
            if((xStart & 0xFFC00000) == (yStart & 0xFFC00000))
                return x.first < y.first ? -1 : x.first == y.first ? 0 : 1;

            // Return SFRs first, then RAM, then Boot Flash, then ordinary flash
            if((xStart & 0x1FC00000) == 0x1F800000) // SFRs
                return -1;
            if((yStart & 0x1FC00000) == 0x1F800000)
                return 1;

            if((xStart & 0x1FC00000) == 0x00000000) // RAM
                return -1;
            if((yStart & 0x1FC00000) == 0x00000000)
                return 1;

            if((xStart & 0x1FC00000) == 0x1FC00000) // Boot Flash and Config Words
                return -1;
            if((yStart & 0x1FC00000) == 0x1FC00000)
                return 1;

            if((xStart & 0x1FC00000) == 0x1D000000) // Regular Flash
                return -1;
            if((yStart & 0x1FC00000) == 0x1D000000)
                return 1;

            // Should never get down here, but if so, compare as ordinary address
            return x.first < y.first ? -1 : x.first == y.first ? 0 : 1;
        }
    }
}
