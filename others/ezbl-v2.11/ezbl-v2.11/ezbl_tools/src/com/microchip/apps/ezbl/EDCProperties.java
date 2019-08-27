/**
 * Copyright (C) 2018 Microchip Technology Inc.
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


/**
 *
 * @author C12128
 */
public class EDCProperties implements Serializable, Cloneable
{
    String partNumber;
    EZBLState.CPUClass coreType = EZBLState.CPUClass.other;
    int programBlockSize;
    int eraseBlockSize;
    long devIDAddr;
    long devIDMask;
    long devID;
    long revIDAddr;
    long revIDMask;
    long dsNum;
    long dosNum;
    boolean hasFlashConfigWords;
    String BACKBUGRegName;
    long BACKBUGAddr;
    int BACKBUGPos;
    long BACKBUGMask;           // Only used/applicable on PIC32
    String BACKBUGAdjAffectedRegName;
    String ReservedBitRegName;
    long ReservedBitAddr;
    int ReservedBitPos;
    String ReservedBitAdjAffectedRegName;
    String CodeProtectRegName;
    long CodeProtectAddr;
    long CodeProtectMask;
    String dataOriginFile;
    long dataOriginDate;

    public EDCProperties()
    {
    }

    public EDCProperties(ResultSet dbData)
    {
        try
        {
            this.partNumber = dbData.getString("part");
            this.coreType = EZBLState.CPUClass.values()[dbData.getInt("CPUClass")];
            this.programBlockSize = dbData.getInt("programBlockSize");
            this.eraseBlockSize = dbData.getInt("eraseBlockSize");
            this.devIDAddr = dbData.getLong("devIDAddr");
            this.devIDMask = dbData.getLong("devIDMask");
            this.devID = dbData.getLong("devID");
            this.revIDAddr = dbData.getLong("revIDAddr");
            this.revIDMask = dbData.getLong("revIDMask");
            this.dsNum = dbData.getLong("dsNum");
            this.dosNum = dbData.getLong("dosNum");
            this.hasFlashConfigWords = dbData.getBoolean("hasFlashConfigWords");
            this.BACKBUGRegName = dbData.getString("BACKBUGRegName");
            this.BACKBUGAddr = dbData.getLong("BACKBUGAddr");
            this.BACKBUGPos = dbData.getInt("BACKBUGPos");
            this.BACKBUGMask = dbData.getLong("BACKBUGMask");
            this.BACKBUGAdjAffectedRegName = dbData.getString("BACKBUGAdjAffectedRegName");
            this.ReservedBitRegName = dbData.getString("ReservedBitRegName");
            this.ReservedBitAddr = dbData.getLong("ReservedBitAddr");
            this.ReservedBitPos = dbData.getInt("ReservedBitPos");
            this.ReservedBitAdjAffectedRegName = dbData.getString("ReservedBitAdjAffectedRegName");
            this.CodeProtectRegName = dbData.getString("CodeProtectRegName");
            this.CodeProtectAddr = dbData.getLong("CodeProtectAddr");
            this.CodeProtectMask = dbData.getLong("CodeProtectMask");
            this.dataOriginFile = dbData.getString("dataOriginFile");
            this.dataOriginDate = dbData.getLong("dataOriginDate");
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static EDCProperties fromCSVLine(String csvFields[])
    {
        int i = 0;
        EDCProperties ret = new EDCProperties();
        // part TEXT
        // CPUClass INTEGER
        // programBlockSize INTEGER
        // eraseBlockSize INTEGER
        // devIDAddr INTEGER
        // devIDMask INTEGER
        // devID INTEGER
        // revIDAddr INTEGER
        // revIDMask INTEGER
        // dsNum INTEGER
        // dosNum INTEGER
        // hasFlashConfigWords INTEGER
        // BACKBUGRegName TEXT
        // BACKBUGAddr INTEGER
        // BACKBUGPos INTEGER
        // BACKBUGMask INTEGER
        // BACKBUGAdjAffectedRegName TEXT
        // ReservedBitRegName TEXT
        // ReservedBitAddr INTEGER
        // ReservedBitPos INTEGER
        // ReservedBitAdjAffectedRegName TEXT
        // CodeProtectRegName TEXT
        // CodeProtectAddr INTEGER
        // CodeProtectMask INTEGER
        // dataOriginFile TEXT
        // dataOriginDate INTEGER
        ret.partNumber = csvFields[i++].isEmpty() ? null : TrimQuotes(csvFields[i - 1]);
        ret.coreType = EZBLState.CPUClass.values()[Integer.decode(csvFields[i++])];
        ret.programBlockSize = Integer.decode(csvFields[i++]);
        ret.eraseBlockSize = Integer.decode(csvFields[i++]);
        ret.devIDAddr = Long.decode(csvFields[i++]);
        ret.devIDMask = Long.decode(csvFields[i++]);
        ret.devID = Long.decode(csvFields[i++]);
        ret.revIDAddr = Long.decode(csvFields[i++]);
        ret.revIDMask = Long.decode(csvFields[i++]);
        ret.dsNum = Long.decode(csvFields[i++]);
        ret.dosNum = Long.decode(csvFields[i++]);
        ret.hasFlashConfigWords = !csvFields[i++].equals("0");
        ret.BACKBUGRegName = csvFields[i++].isEmpty() ? null : TrimQuotes(csvFields[i - 1]);
        ret.BACKBUGAddr = Long.decode(csvFields[i++]);
        ret.BACKBUGPos = Integer.decode(csvFields[i++]);
        ret.BACKBUGMask = Long.decode(csvFields[i++]);
        ret.BACKBUGAdjAffectedRegName = csvFields[i++].isEmpty() ? null : TrimQuotes(csvFields[i - 1]);
        ret.ReservedBitRegName = csvFields[i++].isEmpty() ? null : TrimQuotes(csvFields[i - 1]);
        ret.ReservedBitAddr = Long.decode(csvFields[i++]);
        ret.ReservedBitPos = Integer.decode(csvFields[i++]);
        ret.ReservedBitAdjAffectedRegName = csvFields[i++].isEmpty() ? null : TrimQuotes(csvFields[i - 1]);
        ret.CodeProtectRegName = csvFields[i++].isEmpty() ? null : TrimQuotes(csvFields[i - 1]);
        ret.CodeProtectAddr = Long.decode(csvFields[i++]);
        ret.CodeProtectMask = Long.decode(csvFields[i++]);
        ret.dataOriginFile = csvFields[i++].isEmpty() ? null : TrimQuotes(csvFields[i - 1]);
        ret.dataOriginDate = Long.decode(csvFields[i++]);
        return ret;
    }

    public static EDCProperties fromCSVLine(String csvData)
    {
        return fromCSVLine(csvData.split(","));
    }

    @Override
    public EDCProperties clone()
    {
        try
        {
            return EDCProperties.class.cast(super.clone());
        }
        catch(CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
