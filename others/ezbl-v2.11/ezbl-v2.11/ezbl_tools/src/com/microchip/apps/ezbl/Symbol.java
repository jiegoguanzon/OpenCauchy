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
import java.text.Collator;
import java.util.*;


/**
 *
 * @author C12128
 */
public class Symbol implements Serializable, Cloneable, Comparable<Symbol>
{
    static final long serialVersionUID = 1L;
    public long address = -1;
    public SymbolFlags flags = null;
    public String section = null;
    public long alignment_size = -1;
    public String name = null;
    public String probableFile = null;

    public Symbol()
    {
    }

    public Symbol(String obj_dump_line)
    {
        if((obj_dump_line == null) || (obj_dump_line.length() < 18))
        {
            return;
        }

        //00000000 l    df *ABS*	00000000 C:\Work\mla\TCPIP\Demo App\MPLAB.X\..\..\..\Microchip\TCPIP Stack\UDP.c
        try
        {
            address = Long.parseLong(obj_dump_line.substring(0, 8), 16);
            flags = new SymbolFlags(obj_dump_line.substring(9, 16));
            int alignSpaceOffset = obj_dump_line.indexOf(' ', 17);
            int alignOffset = obj_dump_line.indexOf('\t', 17);
            if(alignOffset == -1 || ((alignSpaceOffset != 1) && (alignSpaceOffset < alignOffset)))  // Should always be a tab, but just in case...
            {
                alignOffset = alignSpaceOffset;
            }
            section = obj_dump_line.substring(17, alignOffset).trim();
            while((obj_dump_line.charAt(alignOffset) == ' ') || (obj_dump_line.charAt(alignOffset) == '\t'))
            {
                alignOffset++;
            }
            alignment_size = Long.parseLong(obj_dump_line.substring(alignOffset, alignOffset + 8), 16);
            name = obj_dump_line.substring(alignOffset + 9);
            int extraTokenEndIndex = name.lastIndexOf(' ');
            if(extraTokenEndIndex >= 0)
                name = name.substring(extraTokenEndIndex + 1);
        }
        catch(Exception ex)
        {
            System.out.printf("ezbl_tools.jar: unable to convert symbol line:"
                              + "\n                \"%1$s\"\n", obj_dump_line);
        }

    }

    public Symbol Clone()
    {
        Symbol ret = new Symbol();

        ret.address = this.address;
        ret.flags = this.flags.Clone();
        ret.section = this.section;
        ret.alignment_size = this.alignment_size;
        ret.name = this.name;
        ret.probableFile = this.probableFile;

        return ret;
    }

    public boolean equals(Symbol symbol, boolean testSection)
    {
        boolean match = (this.address == symbol.address) && this.flags.equals(symbol.flags) && (this.alignment_size == symbol.alignment_size) && this.name.equals(symbol.name);
        if(!match)
        {
            return false;
        }
        if(!testSection)
        {
            return true;
        }
        return this.section.equals(symbol.section);
    }

    @Override
    public int compareTo(Symbol y)
    {
        return this.address == y.address ? 0 : (this.address > y.address ? 1 : -1);
    }

    public void normalizePIC32Addresses()
    {
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
     * Checks if the current symbol is reasonable to expose to the Application
     * project as a weak equ.
     *
     * @return true if all internal conditions are met, false otherwise.
     *         Presently, symbols must meet all of these criteria: - not be
     *         local - must be a function, object, or contain "EZBL" in its name
     *         - name can't start with two or more underscore characters
     */
    public boolean isExportSuitable()
    {
        // Do not export local symbols
        if(this.flags.local)
            return false;

        // Do not export special symbols
        if(this.flags.file || this.flags.relocatableEvaluated || this.flags.warning || this.flags.dynamic || this.flags.indirectReference)
            return false;

        // Do no export undefined (*UND* section) symbols
        if(this.section.equals("*UND*"))
            return false;

        // Do not export absolute symbols unless they contain "EZBL" in the name (useful to suppress SFR address symbols on XC16)
        if(this.section.equals("*ABS*") && !this.name.contains("EZBL"))
            return false;

        // Do not export symbols which can't be encoded as a normal symbol in a .s file (ex: EZBL_ICD_RAM_RESERVE%GAPS, or other ones generated by the compiler/linker, not code)
        if(!this.name.matches("[A-Za-z_][A-Za-z0-9_]*"))
            return false;

        // Do not propagate the EZBL_BOOT_PROJECT symbol from Bootloaders into Applications
        if(this.name.equals("EZBL_BOOT_PROJECT")
           || this.name.equals("EZBL_appBootloadState") // Applies to PIC32MM only - XC16 version has leading underscore) 
           || this.name.startsWith("__linked_"))
            return false;

        return true;
    }

    @Override
    public String toString()
    {
        return String.format("%1$08X %2$s %3$s\t%4$08X %5$s\n", this.address, this.flags, this.section, this.alignment_size, this.name);
    }

    public boolean nameMatchesRegEx(List<String> regExpressions)
    {
        if(regExpressions == null)
            return false;

        for(String regEx : regExpressions)
        {
            if(this.name.matches(regEx))
                return true;
        }

        return false;
    }
    public boolean nameMatches(List<String> literalList)
    {
        if(literalList == null)
            return false;

        for(String testName : literalList)
        {
            if(this.name.matches(testName))
                return true;
        }

        return false;
    }
    public boolean nameStartsWith(List<String> literalList)
    {
        if(literalList == null)
            return false;

        for(String prefix : literalList)
        {
            if(this.name.startsWith(prefix))
                return true;
        }

        return false;
    }

}


/**
 * Compares symbols by alphabetical name, not addr
 */
class SymbolNameComparator implements Comparator<Symbol>
{
    @Override
    public int compare(Symbol x, Symbol y)
    {
        return Collator.getInstance().compare(x.name, y.name);
    }
}
