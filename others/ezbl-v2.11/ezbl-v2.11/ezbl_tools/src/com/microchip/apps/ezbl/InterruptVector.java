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

import static com.microchip.apps.ezbl.Multifunction.TrimQuotes;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;


/**
 *
 * @author C12128
 */
public class InterruptVector implements Serializable, Comparable<InterruptVector>
{
    int vectorNum = 0; // Zero based vector index
    int irqNum = -8; // IRQ number (starts at vector 8, after the trap vectors)
    String name = null; // Vector name without leading underscores unless unimplemented
    String desc = null; // Human descriptive name, ex: "U1TX - UART1 Transmitter"
    boolean trap = false;
    boolean implemented = false;
    long remapAddress = -1;

    public InterruptVector()
    {
    }

    public InterruptVector(ResultSet dbData)
    {
        try
        {
            String parts = dbData.getString("parts").toLowerCase();
            vectorNum = dbData.getInt("vector");
            name = dbData.getString("name");
            implemented = dbData.getBoolean("implemented");
            trap = dbData.getBoolean("trap");
            desc = dbData.getString("desc");
            irqNum = (parts.contains("dspic") || parts.contains("pic24")) ? vectorNum - 8 : vectorNum;
            remapAddress = -1;
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static InterruptVector fromCSVLine(String csvFields[])
    {
        // id INTEGER
        // parts TEXT
        // vector INTEGER
        // name TEXT
        // implemented INTEGER
        // trap INTEGER
        // desc TEXT
        InterruptVector ret = new InterruptVector();
        ret.vectorNum = Integer.decode(csvFields[2]);
        ret.irqNum = (csvFields[1].contains("dsPIC") || csvFields[1].contains("PIC24")) ? ret.vectorNum - 8 : ret.vectorNum;
        ret.name = csvFields[3].isEmpty() ? null : TrimQuotes(csvFields[3]);
        ret.implemented = !csvFields[4].equals("0");
        ret.trap = !csvFields[5].equals("0");
        ret.desc = csvFields[6].isEmpty() ? null : TrimQuotes(csvFields[6]);
        return ret;
    }

    public static InterruptVector fromCSVLine(String csvLine)
    {
        return fromCSVLine(csvLine.split("[,]"));
    }

    public InterruptVector(String name, int vectorNum, boolean implemented, boolean trap, String description)
    {
        this.desc = description;
        this.name = name;
        this.implemented = implemented;
        this.remapAddress = -1;
        this.trap = trap;
        this.irqNum = vectorNum - 8;
        this.vectorNum = vectorNum;
    }

    /**
     * Compares two InterruptVector's by vector number only
     *
     * @return 1 for higher vector numbers, -1 for lower vector number, 0 for
     *         same vector number
     */
    @Override
    public int compareTo(InterruptVector vec)
    {
        return (vec.vectorNum > this.vectorNum) ? 1
               : (vec.vectorNum < this.vectorNum) ? -1
                 : 0;
    }

    public static int GetImplementedVectorCount(Collection<InterruptVector> vectors)
    {
        int ret = 0;
        for(InterruptVector iv : vectors)
        {
            if(iv.implemented)
                ret++;
        }
        return ret;
    }
}
