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

import static com.microchip.apps.ezbl.EDCReader.*;
import static com.microchip.apps.ezbl.Multifunction.*;
import static com.microchip.apps.ezbl.EZBLState.CPUClass;
import com.microchip.apps.ezbl.MemoryRegion.MemType;
import com.microchip.apps.ezbl.MemoryRegion.Partition;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import javax.management.RuntimeErrorException;
import org.w3c.dom.*;


/**
 *
 * @author sqlitetutorial.net
 */
public class SQLitePartDatabase
{
    private String dbFilename = null;
    private Connection db = null;
    private Long sfrID = null;


    private class CachedSFRs
    {
        private TreeMap<String, EZBL_SFR> sfrsByName = new TreeMap<String, EZBL_SFR>();
        private TreeMap<Long, EZBL_SFR> sfrsByAddr = new TreeMap<Long, EZBL_SFR>();
        private String partNum = "";
    };
    CachedSFRs sfrCache = new CachedSFRs();


    public static enum MemoryTable  // For getMemories()/insertMemories()
    {
        DeviceMemories, // All memories found in EDC, undecoded or aligned
        GLDMemories, // All program space memories, aligned to erasable boundaries
        BootloadableRanges, // All program space memories, aligned to erasable boundaries
        DecodedConfigMemories     // All config word memories, split out by individual Config word names (DCRs)
    }

    /**
     * Requires SQLite/db to be opened and the SQLite3 db .jar to be available.
     *
     * Converts the table contents, sorted by the given SQL string, to a .csv
     * format at returns the whole table contents.
     *
     * @param tableName  Database table name, such as: "DeviceProperties",
     *                   "DeviceMemories", "DecodedConfigMemories",
     *                   "GLDMemories", "BootloadableRanges", "Interrupts",
     *                   "SFRs", or "Bitfields".
     * @param orderBySQL SQL ORDER BY clause text, including the "ORDER BY"
     *                   verb. If unneeded, set to null or an empty string.
     *
     * @return
     */
    public String exportTable(String tableName, String orderBySQL)
    {
        List<String> outputText = new ArrayList<String>();
        if(orderBySQL == null)
            orderBySQL = "";
        ResultSet rs = execQuery("SELECT * FROM " + tableName + " " + orderBySQL + ";");
        try
        {
            ResultSetMetaData meta = rs.getMetaData();
            for(int i = 1; i <= meta.getColumnCount(); i++)
            {
                outputText.add(meta.getColumnLabel(i));
                outputText.add(",");
            }
            outputText.set(outputText.size() - 1, "\n");
            while(rs.next())
            {
                for(int i = 1; i <= meta.getColumnCount(); i++)
                {
                    Object colData = rs.getObject(i);
                    if(colData != null)
                    {
                        if(String.class.isInstance(colData))
                            outputText.add("\"" + colData.toString() + "\"");
                        else
                            outputText.add(colData.toString());
                    }
                    outputText.add(",");
                }
                outputText.set(outputText.size() - 1, "\n");
            }
            outputText.remove(outputText.size() - 1);
        }
        catch(SQLException ex)
        {
            throw new RuntimeException(ex);
        }

        return Multifunction.CatStringList(outputText);
    }

    /**
     * Extracts the parts database from the Jar executable to a temporary file
     * on the local file system.
     *
     * @return The path to the temporary file copied out of the Jar. This file
     *         is automatically deleted by the Java JRE if the process closes
     *         normally.
     *
     * @throws RuntimeException If the parts.db file cannot be found in the Jar
     *                          or a IOException occurs when attempting to copy
     *                          it to the local file system.
     */
    public String extractJarResourceToTempFile(String jarResourcePath) throws RuntimeException
    {
        InputStream resourceStream = null;
        String tempExtractedFilePath = null;
        resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(jarResourcePath);
        if(resourceStream == null)
        {
            throw new RuntimeException("Cannot read parts database from Jar file");
        }

        try
        {
            File dbLocalJarPath = new File(jarResourcePath);
            File f = File.createTempFile(dbLocalJarPath.getName(), "");
            f.deleteOnExit();
            tempExtractedFilePath = f.getCanonicalPath();
            Files.copy(resourceStream, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
            resourceStream.close();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }

        return tempExtractedFilePath;
    }
    /**
     * Requires SQLite3 .jar library to be available.
     *
     * Loads a part database from disk to an in-memory SQLite database
     *
     * @param filename Database path including extension
     *
     * @throws RuntimeException if a database error occurs
     */
    public void openDatabase(String filename) throws RuntimeException
    {
        if(db != null)
            saveDatabase(true);

        try
        {
            db = DriverManager.getConnection("jdbc:sqlite:");   // In memory data base only
            if((filename != null) && FileExists(filename))
            {
                dbFilename = filename;
                exec("restore from '" + filename + "'");
            }
            initTables();
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens an in memory database only, equivalent to calling
     * openDatabase(null);
     *
     * If you wish to save the database to a file, set db.dbFilename to the
     * output file path and then call db.saveDatabase();
     */
    public void openDatabase() throws RuntimeException
    {
        openDatabase(null);
    }

    /**
     * Requires SQLite/db to be opened and the SQLite3 db .jar to be available.
     *
     * Saves an in memory database to db.dbFilename and .csv formatted resource
     * text files for later inclusion in the ezbl_tools.jar file.
     *
     * @param closeAfterSave Closes the database after saving, if true.
     *                       Otherwise, more changes or accesses can be made to
     *                       it.
     *
     * @throws RuntimeException if DB error occurs
     */
    public void saveDatabase(boolean closeAfterSave) throws RuntimeException
    {
        if(db == null)
            return;

        String pathToSources = "src/com/microchip/apps/ezbl/";

        Multifunction.UpdateFileIfDataDifferent(pathToSources + "resources/DeviceProperties.txt", exportTable("DeviceProperties", "ORDER BY part ASC"), false);
        Multifunction.UpdateFileIfDataDifferent(pathToSources + "resources/DeviceMemories.txt", exportTable("DeviceMemories", "ORDER BY part ASC, partition ASC, type ASC, startAddr ASC"), false);
        Multifunction.UpdateFileIfDataDifferent(pathToSources + "resources/DecodedConfigMemories.txt", exportTable("DecodedConfigMemories", "ORDER BY part ASC, partition ASC, type ASC, startAddr ASC"), false);
        Multifunction.UpdateFileIfDataDifferent(pathToSources + "resources/GLDMemories.txt", exportTable("GLDMemories", "ORDER BY part ASC, partition ASC, type ASC, startAddr ASC"), false);
        Multifunction.UpdateFileIfDataDifferent(pathToSources + "resources/BootloadableRanges.txt", exportTable("BootloadableRanges", "ORDER BY part ASC, partition ASC, type ASC, startAddr ASC"), false);
        Multifunction.UpdateFileIfDataDifferent(pathToSources + "resources/Interrupts.txt", exportTable("Interrupts", "ORDER BY parts ASC, vector ASC"), false);
        Multifunction.UpdateFileIfDataDifferent("../SFRs.txt", exportTable("SFRs", "ORDER BY parts ASC, address ASC, name ASC"), false);
        Multifunction.UpdateFileIfDataDifferent("../Bitfields.txt", exportTable("Bitfields", "ORDER BY sfrid ASC, position ASC, name ASC"), false);

        compressPartGroupings();

        exec("backup to '" + dbFilename + "'");
        if(closeAfterSave)
        {
            try
            {
                db.close();
            }
            catch(SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            db = null;
        }
    }

    public void compressPartGroupings() throws RuntimeException
    {
        if(db == null)
            return;

        exec("DROP TABLE IF EXISTS UniqueBitfields;");
        exec("CREATE TABLE IF NOT EXISTS UniqueBitfields ("
             + "id INTEGER PRIMARY KEY,"
             + "SFR TEXT,"
             + "BitfieldName TEXT,"
             + "BitfieldPosition INTEGER,"
             + "BitfieldWidth INTEGER,"
             + "Parts TEXT);");

        ResultSet rs = execQuery("SELECT * FROM AllBitfieldsUnion;");
        try
        {
            while(rs.next())
            {
                String sfr = rs.getString("SFR");
                String bitfield = rs.getString("BitfieldName");
                int bitfieldPosition = rs.getInt("BitfieldPosition");
                int bitfieldWidth = rs.getInt("BitfieldWidth");
                String[] rawParts = rs.getString("Parts").split("\\|");
                List<String> parts = Arrays.asList(rawParts);
                Collections.sort(parts);
                String partsString = CatStringList(parts, "|");
                exec("INSERT OR REPLACE INTO UniqueBitfields("
                     + "SFR,"
                     + "BitfieldName,"
                     + "BitfieldPosition,"
                     + "BitfieldWidth,"
                     + "Parts) VALUES(?,?,?,?,?);",
                     sfr, bitfield, bitfieldPosition, bitfieldWidth, partsString);
            }
            rs.close();
        }
        catch(SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public String sqlf(String sql, Object... params)
    {
        List<String> frags = new ArrayList<String>();
        int i = 1;
        int index = -1;
        int lastIndex = -1;
        for(Object o : params)
        {
            i++;
            index = sql.indexOf('?', lastIndex + 1);
            if(index < 0)
                break;

            frags.add(sql.substring(lastIndex + 1, index));

            if(o == null)
                frags.add("NULL");
            else if(Boolean.class.isInstance(o))
                frags.add(Boolean.class.cast(o) ? "1" : "0");
            else if(Byte.class.isInstance(o))
                frags.add(Byte.class.cast(o).toString());
            else if(Integer.class.isInstance(o))
                frags.add(Integer.class.cast(o).toString());
            else if(Long.class.isInstance(o))
                frags.add(Long.class.cast(o).toString());
            else if(String.class.isInstance(o))
                frags.add("\"" + String.class.cast(o) + "\"");
            else if(Float.class.isInstance(o))
                frags.add(Float.class.cast(o).toString());
            else if(Double.class.isInstance(o))
                frags.add(Double.class.cast(o).toString());
            else
                throw new RuntimeException("Unsupported scanf class type for parameter " + i);

            lastIndex = index;
        }

        frags.add(sql.substring(lastIndex + 1));
        return Multifunction.CatStringList(frags);
    }

    /**
     * Requires SQLite/db to be opened and the SQLite3 db .jar to be available.
     *
     * Creates tables for the device database, if not already existent
     */
    public void initTables() throws RuntimeException
    {
        exec("CREATE TABLE IF NOT EXISTS DeviceProperties ("
             + "part TEXT PRIMARY KEY," // Device part number
             + "CPUClass INTEGER,"
             + "programBlockSize INTEGER,"
             + "eraseBlockSize INTEGER,"
             + "devIDAddr INTEGER,"
             + "devIDMask INTEGER,"
             + "devID INTEGER,"
             + "revIDAddr INTEGER,"
             + "revIDMask INTEGER,"
             + "dsNum INTEGER,"
             + "dosNum INTEGER,"
             + "hasFlashConfigWords INTEGER,"
             + "BACKBUGRegName TEXT,"
             + "BACKBUGAddr INTEGER,"
             + "BACKBUGPos INTEGER,"
             + "BACKBUGMask INTEGER,"
             + "BACKBUGAdjAffectedRegName TEXT,"
             + "ReservedBitRegName TEXT,"
             + "ReservedBitAddr INTEGER,"
             + "ReservedBitPos INTEGER,"
             + "ReservedBitAdjAffectedRegName TEXT,"
             + "CodeProtectRegName TEXT,"
             + "CodeProtectAddr INTEGER,"
             + "CodeProtectMask INTEGER,"
             + "dataOriginFile TEXT,"
             + "dataOriginDate INTEGER);");
        exec("CREATE TABLE IF NOT EXISTS DeviceMemories ("
             + "id INTEGER PRIMARY KEY,"
             + "part TEXT,"
             + "type INTEGER," // MemoryRegion.MemType enum ordinal
             + "partition INTEGER," // MemoryRegion.Partition enum ordinal
             + "name TEXT,"
             + "startAddr INTEGER,"
             + "endAddr INTEGER,"
             + "programAlign INTEGER,"
             + "eraseAlign INTEGER);");
        exec("CREATE TABLE IF NOT EXISTS DecodedConfigMemories ("
             + "id INTEGER PRIMARY KEY,"
             + "part TEXT,"
             + "type INTEGER," // MemoryRegion.MemType enum ordinal
             + "partition INTEGER," // MemoryRegion.Partition enum ordinal
             + "name TEXT,"
             + "startAddr INTEGER,"
             + "endAddr INTEGER,"
             + "programAlign INTEGER,"
             + "eraseAlign INTEGER"
             + ");");
        exec("CREATE TABLE IF NOT EXISTS GLDMemories ("
             + "id INTEGER PRIMARY KEY,"
             + "part TEXT,"
             + "type INTEGER," // MemoryRegion.MemType enum ordinal
             + "partition INTEGER," // MemoryRegion.Partition enum ordinal
             + "name TEXT,"
             + "startAddr INTEGER,"
             + "endAddr INTEGER,"
             + "programAlign INTEGER,"
             + "eraseAlign INTEGER);");
        exec("CREATE TABLE IF NOT EXISTS BootloadableRanges ("
             + "id INTEGER PRIMARY KEY,"
             + "part TEXT,"
             + "type INTEGER," // MemoryRegion.MemType enum ordinal
             + "partition INTEGER," // MemoryRegion.Partition enum ordinal
             + "name TEXT,"
             + "startAddr INTEGER,"
             + "endAddr INTEGER,"
             + "programAlign INTEGER,"
             + "eraseAlign INTEGER);");
        exec("CREATE TABLE IF NOT EXISTS Interrupts ("
             + "id INTEGER PRIMARY KEY,"
             + "parts TEXT,"
             + "vector INTEGER,"
             + "name TEXT,"
             + "implemented INTEGER,"
             + "trap INTEGER,"
             + "desc TEXT);");
        exec("CREATE TABLE IF NOT EXISTS SFRs ("
             + "sfrid INTEGER PRIMARY KEY,"
             + "parts TEXT,"
             + "address INTEGER,"
             + "name TEXT,"
             + "desc TEXT,"
             + "parentmodule TEXT,"
             + "hash TEXT);");
        exec("CREATE TABLE IF NOT EXISTS Bitfields ("
             + "bitfieldid INTEGER PRIMARY KEY,"
             + "sfrid INTEGER NOT NULL,"
             + "position INTEGER,"
             + "width INTEGER,"
             + "name TEXT,"
             + "desc TEXT,"
             + "hidden INTEGER,"
             + "FOREIGN KEY (sfrid) REFERENCES SFRs(sfrid));");

        TreeMap<String, String> IntPMDBitfieldRegEx = new TreeMap<>();
        IntPMDBitfieldRegEx.put("UART", "^U[0-9]+((MD)|([RT]XI.+)|(EVTI.+)|(EI.+))");
        IntPMDBitfieldRegEx.put("I2C", "^[SM]?I2C[0-9]+((MD)|(I.+)|(BCI.+))");
        IntPMDBitfieldRegEx.put("SPI", "^SPI[0-9]+((MD)|(I.+)|(EI.+)|([TR]XI.+))");
        IntPMDBitfieldRegEx.put("CAN", "^C[0-9]+((MD)|([RT]XI.+)|(I[^N]+))");
        IntPMDBitfieldRegEx.put("USB", "^USB[0-9]+((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("Timer", "^T[0-9]+((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("CCT", "^CCT[0-9]+((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("CCP", "^CCP[0-9]+((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("IC", "^IC[0-9]+((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("OC", "^OC[0-9]+((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("CLC", "^CLC[0-9]+((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("DMA", "^DMA[0-9]+((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("REFO", "^REFO[0-9]?((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("LVD", "^LVD[0-9]?((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("ADC", "^ADC?[0-9]?((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("CRC", "^CRC[0-9]?((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("CMP", "^CMP[0-9]?((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("CTMU", "^CTMU[0-9]?((MD)|(I.+))");
        IntPMDBitfieldRegEx.put("RTCC", "^RTCC[0-9]?((MD)|(I.+))");

        for(String peripheral : IntPMDBitfieldRegEx.keySet())
        {
            exec("CREATE VIEW IF NOT EXISTS IntPMDBitfields" + peripheral + " AS SELECT SFRs.sfrid, SFRs.parts, SFRs.address, SFRs.name, SFRs.desc, SFRs.parentmodule, Bitfields.name AS BitfieldName, position AS BitfieldPosition, width AS BitfieldWidth, Bitfields.desc AS BitfieldDesc, hidden AS BitFieldHidden FROM SFRs CROSS JOIN Bitfields WHERE SFRs.sfrid == Bitfields.sfrid AND (Bitfields.name REGEXP '" + IntPMDBitfieldRegEx.get(peripheral) + "') ORDER BY Bitfields.name, SFRs.name, Bitfields.position, Bitfields.width;");
            exec("CREATE VIEW IF NOT EXISTS Union" + peripheral + " AS SELECT name AS SFR, BitfieldName, BitfieldPosition, BitfieldWidth, group_concat(parts, '') AS Parts FROM IntPMDBitfields" + peripheral + " GROUP BY SFR, BitfieldName, BitfieldPosition, BitfieldWidth ORDER BY BitfieldName, BitfieldPosition, BitfieldWidth, SFR, Parts;");
        }

        exec("CREATE VIEW IF NOT EXISTS AllBitfields           AS SELECT SFRs.sfrid, SFRs.parts, SFRs.address, SFRs.name, SFRs.desc, SFRs.parentmodule, Bitfields.name AS BitfieldName, position AS BitfieldPosition, width AS BitfieldWidth, Bitfields.desc AS BitfieldDesc, hidden AS BitFieldHidden FROM SFRs CROSS JOIN Bitfields WHERE SFRs.sfrid == Bitfields.sfrid ORDER BY Bitfields.name, SFRs.name, Bitfields.position, Bitfields.width;");
        exec("CREATE VIEW IF NOT EXISTS AllBitfieldsUnion      AS SELECT name AS SFR, BitfieldName, BitfieldPosition, BitfieldWidth, group_concat(parts, '') AS Parts FROM AllBitfields GROUP BY SFR, BitfieldName, BitfieldPosition, BitfieldWidth ORDER BY BitfieldName, BitfieldPosition, BitfieldWidth, SFR, Parts;");
        exec("CREATE VIEW IF NOT EXISTS AllUniquePartGroupings AS SELECT DISTINCT Parts FROM UniqueBitfields ORDER BY Parts;");

    }

    /**
     * Reads the list of PIC devices and the File paths used to obtain part
     * information pre-stored within the SQLite3 database (if opened), or the
     * internally saved .csv formatted resource data from this .jar executable.
     *
     * @return Sorted mapping of full part numbers (ex: "dsPIC33EP512MU810") and
     *         the XML/.PIC file path the data for the part was originally read
     *         from (ex: "C:\Program Files
     *         (x86)\Microchip\MPLABX\packs\...\dsPIC33EP512MU810.PIC")
     */
    public TreeMap<String, EDCProperties> getParts()
    {
        TreeMap<String, EDCProperties> ret = new TreeMap<>();

        if(db == null)
        {
            String rows[] = Multifunction.ReadJarResource(null, "resources/DeviceProperties.txt").split("[\n]");
            for(int i = 1; i < rows.length; i++)
            {
                EDCProperties prop = EDCProperties.fromCSVLine(rows[i]);
                ret.put(prop.partNumber, prop);
            }
            return null;
        }

        try
        {
            ResultSet rs = execQuery("SELECT * FROM DeviceProperties;");
            while(rs.next())
            {
                EDCProperties prop = new EDCProperties(rs);
                ret.put(prop.partNumber, prop);
            }
            rs.close();

            return ret;
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Requires SQLite/db to be opened and the SQLite3 db .jar to be available.
     *
     * Adds to or updates the list of full part numbers and their .PIC XML file
     * paths in the database. Additionally, the last modified filesystem date
     * for the .PIC XML file is stored or updated in the database.
     *
     * @param parts Mapping of full part numbers (ex: "dsPIC33EP512MU810") and
     *              the XML/.PIC file path the data for the part that will be
     *              read from (ex: "C:\Program Files
     *              (x86)\Microchip\MPLABX\packs\...\dsPIC33EP512MU810.PIC")
     */
    public void insertParts(TreeMap<String, File> parts)
    {
        for(String partNum : parts.keySet())
        {
            File f = parts.get(partNum);
            String dataOriginFile = Multifunction.GetCanonicalPath(f);
            long dataOriginDate = f.lastModified();

            int rowsChanged = exec("UPDATE DeviceProperties SET dataOriginFile = ? WHERE part = ?;", dataOriginFile, partNum);
            if(rowsChanged == 0)
            {
                exec("INSERT INTO DeviceProperties(part, dataOriginFile, dataOriginDate) VALUES(?,?,?);",
                     partNum, dataOriginFile, dataOriginDate);
            }
        }
    }

    /**
     * Obtains general part parameters as an EDCProperties structure for the
     * specified full part number. This function will either read from the
     * SQLite3 database, or if not opened, from the pre-stored .jar resource
     * files.
     *
     * @param partNum Full part number of the device to obtain the properties on
     *                (ex: "dsPIC33EP512MU810")
     *
     * @return New EDCProperties class, populated with all DeviceProperties
     *         table values for the selected part number, either read as a .csv
     *         formatted internal resource or from an external SQLite3 database.
     */
    public EDCProperties getDeviceProperties(String partNum)
    {
        if(db == null)
        {
            partNum = "\"" + partNum + "\"";
            String rows[] = Multifunction.ReadJarResource(null, "resources/DeviceProperties.txt").split("[\n]");
            for(int i = 1; i < rows.length; i++)
            {
                if(rows[i].startsWith(partNum))
                {
                    return EDCProperties.fromCSVLine(rows[i]);
                }
            }
            return null;
        }

        try
        {
            ResultSet rs = execQuery("SELECT * FROM DeviceProperties WHERE part = ?;", partNum);
            if(!rs.next())
            {
                rs.close();
                return null;
            }

            EDCProperties p = new EDCProperties(rs);
            rs.close();
            return p;
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void insertDeviceProperties(EDCProperties properties, boolean clearRelatedData)
    {
        if(clearRelatedData)
            removeDevice(properties.partNumber, clearRelatedData);

        exec("INSERT OR REPLACE INTO DeviceProperties("
             + "part,"
             + "CPUClass,"
             + "programBlockSize,"
             + "eraseBlockSize,"
             + "devIDAddr,"
             + "devIDMask,"
             + "devID,"
             + "revIDAddr,"
             + "revIDMask,"
             + "dsNum,"
             + "dosNum,"
             + "hasFlashConfigWords,"
             + "BACKBUGRegName,"
             + "BACKBUGAddr,"
             + "BACKBUGPos,"
             + "BACKBUGMask,"
             + "BACKBUGAdjAffectedRegName,"
             + "ReservedBitRegName,"
             + "ReservedBitAddr,"
             + "ReservedBitPos,"
             + "ReservedBitAdjAffectedRegName,"
             + "CodeProtectRegName,"
             + "CodeProtectAddr,"
             + "CodeProtectMask,"
             + "dataOriginFile,"
             + "dataOriginDate"
             + ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);",
             properties.partNumber,
             properties.coreType.ordinal(),
             properties.programBlockSize,
             properties.eraseBlockSize,
             properties.devIDAddr,
             properties.devIDMask,
             properties.devID,
             properties.revIDAddr,
             properties.revIDMask,
             properties.dsNum,
             properties.dosNum,
             properties.hasFlashConfigWords,
             properties.BACKBUGRegName,
             properties.BACKBUGAddr,
             properties.BACKBUGPos,
             properties.BACKBUGMask,
             properties.BACKBUGAdjAffectedRegName,
             properties.ReservedBitRegName,
             properties.ReservedBitAddr,
             properties.ReservedBitPos,
             properties.ReservedBitAdjAffectedRegName,
             properties.CodeProtectRegName,
             properties.CodeProtectAddr,
             properties.CodeProtectMask,
             properties.dataOriginFile,
             properties.dataOriginDate);
    }

    public void removeDevice(String partNum, boolean removeRelatedTableData)
    {
        try
        {
            exec("DELETE FROM DeviceProperties WHERE part = ?;", partNum);
            if(removeRelatedTableData)
            {
                exec("DELETE FROM DeviceMemories WHERE part = ?;", partNum);
                exec("DELETE FROM DecodedConfigMemories WHERE part = ?;", partNum);
                exec("DELETE FROM GLDMemories WHERE part = ?;", partNum);
                exec("DELETE FROM BootloadableRanges WHERE part = ?;", partNum);
                ResultSet rs = execQuery("SELECT id,parts FROM Interrupts WHERE parts LIKE ?;", "%" + partNum + "|%");
                if(rs.next())
                {
                    String parts = rs.getString("parts").replace(partNum + "|", "");
                    if(parts.isEmpty())
                        exec("DELETE FROM Interrupts WHERE id = ?;", rs.getLong("id"));
                    else
                        exec("UPDATE Interrupts SET parts = ? WHERE id = ?;", parts, rs.getLong("id"));
                }
                rs.close();
            }
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Double getSingleDouble(String sqlStatement) throws RuntimeException
    {
        return Double.class.cast(getSingle(sqlStatement));
    }
    public double getSingleDouble(String sqlStatement, double defaultIfNull) throws RuntimeException
    {
        Double ret = Double.class.cast(getSingle(sqlStatement));
        return ret == null ? defaultIfNull : ret;
    }
    public Float getSingleFloat(String sqlStatement) throws RuntimeException
    {
        return Float.class.cast(getSingle(sqlStatement));
    }
    public float getSingleFloat(String sqlStatement, float defaultIfNull) throws RuntimeException
    {
        Float ret = Float.class.cast(getSingle(sqlStatement));
        return ret == null ? defaultIfNull : ret;
    }

    public String getSingleString(String sqlStatement) throws RuntimeException
    {
        return String.class.cast(getSingle(sqlStatement));
    }
    public String getSingleString(String sqlStatement, String defaultIfNull) throws RuntimeException
    {
        String ret = String.class.cast(getSingle(sqlStatement));
        return ret == null ? defaultIfNull : ret;
    }

    public Integer getSingleInt(String sqlStatement) throws RuntimeException
    {
        return Integer.class.cast(getSingle(sqlStatement));
    }
    public int getSingleInt(String sqlStatement, int defaultIfNull) throws RuntimeException
    {
        Integer ret = Integer.class.cast(getSingle(sqlStatement));
        return ret == null ? defaultIfNull : ret;
    }
    public Long getSingleLong(String sqlStatement) throws RuntimeException
    {
        Object ret = getSingle(sqlStatement);
        if(ret == null)
            return null;
        if(Integer.class.isInstance(ret))
            return Integer.class.cast(ret).longValue();
        return Long.class.cast(ret);
    }
    public long getSingleLong(String sqlStatement, long defaultIfNull) throws RuntimeException
    {
        Long ret = getSingleLong(sqlStatement);
        return ret == null ? defaultIfNull : ret;
    }

    public Boolean getSingleBoolean(String sqlStatement) throws RuntimeException
    {
        return Boolean.class.cast(getSingle(sqlStatement));
    }
    public boolean getSingleBoolean(String sqlStatement, boolean defaultIfNull) throws RuntimeException
    {
        Boolean ret = Boolean.class.cast(getSingle(sqlStatement));
        return ret == null ? defaultIfNull : ret;
    }

    public Object getSingle(String sqlStatement, Object defaultIfNull) throws RuntimeException
    {
        Object ret = getSingle(sqlStatement);
        return ret == null ? defaultIfNull : ret;
    }

    public Object getSingle(String sqlStatement) throws RuntimeException
    {
        Object ret = null;
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery(sqlStatement);
            if(rs.next())
                ret = rs.getObject(1);
            rs.close();
            stmt.close();
            return ret;
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Queries the database and returns a ResultSet from the database. If no
     * data was returned, the result set has no data in it.
     *
     * @param sqlStatement SQL query string
     *
     * @return ResultSet corresponding to the executeQuery() function for the
     *         database statement.
     *
     * Call close() on the returned ResultSet when no longer needed to free up
     * the database resources associated with it.
     *
     * @throws RuntimeException on any SQLException
     */
    public ResultSet execQuery(String sqlFormat, Object... args) throws RuntimeException
    {
        try
        {
            Statement stmt = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
            ResultSet ret = stmt.executeQuery(sqlf(sqlFormat, args));
            stmt.closeOnCompletion();
            return ret;
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a SQL query or statement against the database, substituting '?'
     * characters in the SQL formatting string with escaped arguments.
     *
     * @return null for statements or queries that return no data. A ResultSet
     *         reference otherwise. The associated Statement has the
     *         closeOnCompletion() function called.
     * @throws RuntimeException on any SQLException
     */
    public int exec(String sqlFormat, Object... args) throws RuntimeException
    {
        try
        {
            Statement stmt = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
            boolean hasResults = stmt.execute(sqlf(sqlFormat, args));
            if(hasResults)
            {
                stmt.close();
                return -1;

            }
            int updateCount = stmt.getUpdateCount();
            stmt.close();
            return updateCount;
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void insertSFRs(String partNum, Collection<EZBL_SFR> sfrs)
    {
        for(EZBL_SFR sfr : sfrs)
        {
            insertSFR(partNum, sfr);
        }
    }

    public void insertSFR(String partNum, EZBL_SFR sfr)
    {
        long primarySFRId;
        String existingParts;
        String sfrHash = sfr.getSFRStructureHashAsString();
        try
        {
            ResultSet rs = execQuery("SELECT sfrid,parts FROM SFRs WHERE hash = ? LIMIT 1;", sfrHash);
            if(rs.next())
            {
                existingParts = rs.getString("parts");
                if(existingParts.contains(partNum + "|"))
                {
                    rs.close();
                    return; // Already exists and is unchanged
                }

                exec("UPDATE SFRs SET parts = ? WHERE sfrid = ?;", existingParts + partNum + "|", rs.getLong("sfrid"));
                rs.close();
                return;
            }
            rs.close();
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }

        if(sfrID == null)
            sfrID = getSingleLong("SELECT MAX(sfrid) FROM SFRs;", -1L) + 1L;

        exec("INSERT INTO SFRs(sfrid,parts,address,name,desc,parentmodule,hash) VALUES(?,?,?,?,?,?,?);",
             ++sfrID, partNum + "|", sfr.addr, sfr.name, sfr.desc, sfr.srcModule, sfrHash);
        primarySFRId = sfrID;
        for(String alias : sfr.aliases)
        {
            exec("INSERT OR REPLACE INTO SFRs(sfrid,parts,address,name,desc,parentmodule,hash) VALUES(?,?,?,?,?,?,?);",
                 ++sfrID, partNum + "|", sfr.addr, alias, sfr.desc, sfr.srcModule, sfrHash);
        }

        for(BitField bf : sfr.bitfields)
        {
            exec("INSERT OR REPLACE INTO Bitfields(sfrid,position,width,name,desc,hidden) VALUES(?,?,?,?,?,?);",
                 primarySFRId, bf.position, bf.width, bf.name, bf.desc, bf.isHidden);
            for(String alias : bf.aliases)
            {
                exec("INSERT OR REPLACE INTO Bitfields(sfrid,position,width,name,desc,hidden) VALUES(?,?,?,?,?,?);",
                     primarySFRId, bf.position, bf.width, alias, bf.desc, bf.isHidden);
            }
        }
    }

    public EZBL_SFR getSFR(String partNum, String sfrName)
    {
        if((sfrCache.partNum == partNum) || (db != null))
            return getSFRs(partNum).get(sfrName);

        String sfrText = Multifunction.ReadJarResource(null, "resources/SFRs.txt");
        String bitfieldText = Multifunction.ReadJarResource(null, "resources/Bitfields.txt");
        String sfrRows[] = SimpleSplit(sfrText, "\n");
        String bitfieldRows[] = SimpleSplit(bitfieldText, "\n");
        for(int i = 1; i < sfrRows.length; i++)
        {
            if(!sfrRows[i].contains(sfrName))
                continue;
            String fields[] = SimpleSplit(sfrRows[i], ",");
            if(!fields[1].contains(partNum))
                continue;

            return new EZBL_SFR(fields, bitfieldRows);
        }
        return null;
    }

    public TreeMap<Long, EZBL_SFR> getSFRsByAddr(String partNum)
    {
        getSFRs(partNum);
        return sfrCache.sfrsByAddr;
    }

    public TreeMap<String, EZBL_SFR> getSFRs(String partNum)
    {
        EZBL_SFR sfr;

        if(sfrCache.partNum.equals(partNum))
            return sfrCache.sfrsByName;
        sfrCache.partNum = partNum;
        sfrCache.sfrsByAddr = new TreeMap<Long, EZBL_SFR>();
        sfrCache.sfrsByName = new TreeMap<String, EZBL_SFR>();

        if(db == null)
        {
            partNum = partNum + "|";
            String sfrText = Multifunction.ReadJarResource(null, "resources/SFRs.txt");
            String bitfieldText = Multifunction.ReadJarResource(null, "resources/Bitfields.txt");
            if((sfrText == null) || (bitfieldText == null))
                return sfrCache.sfrsByName; // Empty
            String sfrRows[] = SimpleSplit(sfrText, "\n");
            String bitfieldRows[] = SimpleSplit(bitfieldText, "\n");

            for(int i = 1; i < sfrRows.length; i++)
            {
                if(!sfrRows[i].contains(partNum))
                    continue;
                String fields[] = SimpleSplit(sfrRows[i], ",");
                if(!fields[1].contains(partNum))
                    continue;

                sfr = new EZBL_SFR(fields, bitfieldRows);
                if(sfrCache.sfrsByAddr.containsKey(sfr.addr))
                {
                    sfrCache.sfrsByAddr.get(sfr.addr).aliases.add(sfr.name);
                    sfrCache.sfrsByName.put(sfr.name, sfrCache.sfrsByAddr.get(sfr.addr));
                    continue;
                }
                sfrCache.sfrsByName.put(sfr.name, sfr);
                sfrCache.sfrsByAddr.put(sfr.addr, sfr);
            }

            return sfrCache.sfrsByName;
        }

        try
        {
            ResultSet rs = execQuery("SELECT * FROM SFRs WHERE parts LIKE ?;", "%" + partNum + "|%");

            while(rs.next())
            {
                long sfrid = rs.getLong("sfrid");
                sfr = new EZBL_SFR();
                sfr.addr = rs.getLong("address");
                if(sfrCache.sfrsByAddr.containsKey(sfr.addr))
                {
                    sfrCache.sfrsByAddr.get(sfr.addr).aliases.add(rs.getString("name"));
                    continue;
                }
                sfr.desc = rs.getString("desc");
                sfr.endAddr = sfr.addr + 2;
                sfr.name = rs.getString("name");
                sfr.srcModule = rs.getString("parentmodule");
                ResultSet bfRS = execQuery("SELECT * FROM Bitfields WHERE sfrid = ? ORDER BY position ASC;", sfrid);
                while(bfRS.next())
                {
                    BitField bf = new BitField();
                    bf.position = bfRS.getInt("position");
                    bf.width = bfRS.getInt("width");
                    bf.name = bfRS.getString("name");
                    bf.desc = bfRS.getString("desc");
                    bf.isHidden = bfRS.getBoolean("hidden");
                    bf.parentSFR = sfr;
                    sfr.bitfieldByName.put(bf.name, bf);
                    sfr.bitfields.add(bf);
                    sfr.endAddr = sfr.addr + (bf.position + bf.width + 7) / 8;
                }
                bfRS.close();
                sfrCache.sfrsByName.put(sfr.name, sfr);
                sfrCache.sfrsByAddr.put(sfr.addr, sfr);
            }
            rs.close();
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }

        return sfrCache.sfrsByName;
    }

    public void insertMemories(MemoryTable table, String partNum, Collection<MemoryRegion> memories)
    {
        for(MemoryRegion mem : memories)
        {
            insertMemory(table, partNum, mem);
        }
    }
    public void insertMemory(MemoryTable table, String partNum, MemoryRegion mem)
    {
        exec("INSERT OR REPLACE INTO ? (part,type,partition,name,startAddr,endAddr,programAlign,eraseAlign) VALUES(?,?,?,?,?,?,?,?)",
             table.toString(), partNum, mem.type.ordinal(), mem.partition.ordinal(), mem.name, mem.startAddr, mem.endAddr, mem.programAlign, mem.eraseAlign);
    }

    public List<MemoryRegion> getMemories(String partNum, MemType type) throws RuntimeException
    {
        return getMemories(partNum, type, Partition.single);
    }

    /**
     * Returns a list of the memory regions of the specified type for the
     * specified PIC part number.
     */
    public List<MemoryRegion> getMemories(String partNum, MemType type, Partition partition) throws RuntimeException
    {
        return getMemories(MemoryTable.DeviceMemories, partNum, type, partition);
    }

    public List<MemoryRegion> getMemories(MemoryTable table, String partNum, MemType type, Partition partition) throws RuntimeException
    {
        List<MemoryRegion> memories = new ArrayList<>();

        if(db == null)
        {
            String rows[] = Multifunction.ReadJarResource(null, "resources/" + table.toString() + ".txt").split("[\n]");
            if(partNum != null)
                partNum = ",\"" + partNum + "\",";
            for(int i = 1; i < rows.length; i++)
            {
                if((partNum == null) || rows[i].contains(partNum))
                {
                    String fields[] = rows[i].split("[,]");
                    if(type != null)
                    {
                        if(Integer.decode(fields[2]) != type.ordinal())
                            continue;
                    }
                    if(partition != null)
                    {
                        if(Integer.decode(fields[3]) != partition.ordinal())
                            continue;
                    }
                    memories.add(MemoryRegion.fromCSVLine(fields));
                }
            }
            Collections.sort(memories);
            return memories;
        }

        try
        {
            String sql = "SELECT name,partition,type,startAddr,endAddr,programAlign,eraseAlign FROM " + table.toString();
            if((partNum != null) || (type != null) || (partition != null))
                sql += " WHERE ";
            if(partNum != null)
                sql += "part = \"" + partNum + "\"" + (((type != null) || (partition != null)) ? " AND " : " ");
            if(type != null)
                sql += "type = " + String.valueOf(type.ordinal()) + ((partition != null) ? " AND " : " ");
            if(partition != null)
                sql += "partition = " + String.valueOf(partition.ordinal());
            sql += " ORDER BY partition ASC, type ASC, startAddr ASC;";

            ResultSet rs = execQuery(sql);
            while(rs.next())
            {
                MemoryRegion mem = new MemoryRegion();
                mem.name = rs.getString("name");
                mem.startAddr = rs.getLong("startAddr");
                mem.endAddr = rs.getLong("endAddr");
                mem.partition = Partition.values()[rs.getInt("partition")];
                mem.type = MemType.values()[rs.getInt("type")];
                mem.programAlign = rs.getInt("programAlign");
                mem.eraseAlign = rs.getInt("eraseAlign");
                memories.add(mem);
            }
            rs.close();

            Collections.sort(memories);
            return memories;
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void insertInterrupt(String partNum, Collection<InterruptVector> interrupts)
    {
        for(InterruptVector iv : interrupts)
        {
            insertInterrupt(partNum, iv);
        }
    }

    public void insertInterrupt(String partNum, InterruptVector interrupt)
    {
        try
        {
            ResultSet rs = execQuery("SELECT id, parts FROM Interrupts WHERE name = ? AND vector = ? AND implemented = ? AND trap = ? AND desc = ? LIMIT 1;", interrupt.name, interrupt.vectorNum, interrupt.implemented, interrupt.trap, interrupt.desc);
            if(rs.next())
            {
                String existingParts = rs.getString("parts");
                if(existingParts.contains(partNum + "|"))
                {// Already exists and is unchanged
                    rs.close();
                    return;
                }

                exec("UPDATE Interrupts SET parts = ? WHERE id = ?;", existingParts + partNum + "|", rs.getLong("id"));
                rs.close();
                return;
            }
            rs.close();
            exec("INSERT INTO Interrupts(parts,name,vector,implemented,trap,desc) VALUES(?,?,?,?,?,?);",
                 partNum + "|", interrupt.name, interrupt.vectorNum, interrupt.implemented, interrupt.trap, interrupt.desc);
        }
        catch(SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a list of the memory regions of the specified type for the
     * specified PIC part number.
     */
    public SortedMap<Integer, InterruptVector> getInterrupts(String partNum) throws RuntimeException
    {
        SortedMap<Integer, InterruptVector> vectors = new TreeMap<Integer, InterruptVector>();
        SortedMap<Integer, InterruptVector> missingVectors = new TreeMap<Integer, InterruptVector>();

        if(db == null)
        {
            partNum = partNum + "|";
            String rows[] = Multifunction.ReadJarResource(null, "resources/Interrupts.txt").split("[\n]");
            for(int i = 1; i < rows.length; i++)
            {
                String fields[] = rows[i].split(",");
                if(fields[1].contains(partNum))
                {
                    InterruptVector iv = InterruptVector.fromCSVLine(fields);
                    vectors.put(iv.vectorNum, iv);
                }
            }
        }
        else
        {
            try
            {
                ResultSet rs = execQuery("SELECT * FROM Interrupts WHERE parts LIKE ? ORDER BY vector ASC, name ASC;", "%" + partNum + "|%");
                while(rs.next())
                {
                    InterruptVector iv = new InterruptVector(rs);
                    vectors.put(iv.vectorNum, iv);
                }
                rs.close();
            }
            catch(SQLException e)
            {
                throw new RuntimeException(e);
            }
        }

        int lastSeenVec = -1;
        for(Integer v : vectors.keySet())
        {
            for(int i = lastSeenVec + 1; i < v; i++)
            {
                missingVectors.put(i, new InterruptVector("_Interrupt" + String.valueOf(i), i, false, i < 8 && (partNum.toLowerCase().startsWith("dspic") || partNum.toLowerCase().startsWith("pic24")), "Reserved"));
            }

            InterruptVector iv = vectors.get(v);

            if(iv.name.startsWith("_Reserved") || iv.name.startsWith("Reserved"))
            {
                iv.name = "_Interrupt" + String.valueOf(v);
                iv.implemented = false;
            }
            lastSeenVec = v;
        }
        vectors.putAll(missingVectors);

        return vectors;
    }

    /**
     * Read edc (Essential Device Characteristics) XML information and converts
     * data needed for EZBL to a SQLite3 database
     *
     * @param args the command line arguments. -path_to_ide_bin must be set
     */
    public static int main(String[] args)
    {
        SQLitePartDatabase db;
        String pathToMPLAB = "./";
        String outputFilename = null;
        TreeMap<String, File> picFiles = new TreeMap<String, File>();
        Collection<File> files;

        Document doc;
        DeviceRegs configRegs = null;
        EZBL_SFR sfr;
        Node node;
        PICDevice pic;
        long startTime = System.currentTimeMillis();
        int partsProcessed = 0;

        for(String arg : args)
        {
            if(arg.startsWith("-output="))
                outputFilename = GetCanonicalPath(TrimQuotes(arg.substring(8)));
            else if(arg.startsWith("-path_to_mplab="))                // Path to IDE for finding .pic files
                pathToMPLAB = GetCanonicalPath(TrimQuotes(arg.substring("-path_to_mplab=".length())));
        }

        db = new SQLitePartDatabase();
        db.openDatabase();
        db.dbFilename = outputFilename;
        db.initTables();

        files = FindFilesRegEx(pathToMPLAB, ".*?(DSPIC|PIC24|PIC32MM|dspic|pic24|pic32mm)[A-Za-z0-9]+?[\\.][Pp][Ii][Cc]$", true);
        if(files.isEmpty())
        {
            System.err.printf("No .pic files found in: \"" + pathToMPLAB + "\"\n");
            return -1;
        }

        for(File f : files)
        {
            String partNum = f.getName().substring(0, f.getName().length() - 4);
            if(partNum.toUpperCase().startsWith("DS"))
                partNum = "ds" + partNum.substring(2);
            if(partNum.toUpperCase().startsWith("AC") || partNum.toUpperCase().contains("_AS_")) // Ignore things like "AC244022_AS_PIC24FJ128GA010.PIC"
                continue;
            if(partNum.startsWith("dsPIC33CH128RA"))
                continue;
            picFiles.put(partNum, f);
        }
        db.insertParts(picFiles);

        for(String part : picFiles.keySet())
        {
            EDCProperties p = new EDCProperties();
            List<MemoryRegion> devAllMemories = new ArrayList<>();
            List<MemoryRegion> devConfigDCRMemories = new ArrayList<>();
            List<MemoryRegion> devBootloadableRanges = new ArrayList<>();
            List<MemoryRegion> devGLDMemories = new ArrayList<>();
            List<InterruptVector> devInterrupts = new ArrayList<>();

            p.partNumber = part;
            p.dataOriginFile = Multifunction.GetCanonicalPath(picFiles.get(part));
            p.dataOriginDate = picFiles.get(part).lastModified();
            System.out.printf("%4.3f: Processing %s (%s)\n", (System.currentTimeMillis() - startTime) / 1000.0, p.partNumber, p.dataOriginFile);
            startTime = System.currentTimeMillis();

            // Find the appropraite EDC .PIC xml file for the target device
            doc = PICXMLLoader.LoadPICXML(picFiles.get(part));
            if(doc == null)
            {
                doc = PICXMLLoader.LoadPICXML(pathToMPLAB + "/../../", p.partNumber);
                if(doc == null)
                {
                    System.err.printf("ezbl_tools: could not load device characteristics for '%s'\n", p.partNumber);
                    continue;
                }
            }

            p.coreType = p.partNumber.startsWith("PIC24F") || p.partNumber.startsWith("PIC24H") || p.partNumber.startsWith("dsPIC30") || p.partNumber.startsWith("dsPIC33F") ? CPUClass.f
                         : p.partNumber.startsWith("PIC32MM") ? CPUClass.mm
                           : p.partNumber.startsWith("PIC24E") || p.partNumber.startsWith("dsPIC33E") ? CPUClass.e
                             : p.partNumber.startsWith("dsPIC33C") ? CPUClass.c
                               : p.partNumber.startsWith("dsPIC33B") ? CPUClass.b
                                 : p.partNumber.startsWith("dsPIC33A") ? CPUClass.a
                                   : CPUClass.other;

            pic = new EDCReader.PICDevice(p.partNumber);
            DeviceRegs regs = IndexSFRDCRNodes(doc.getElementsByTagName("edc:SFRDef"));
            pic.addAll(regs);
            db.insertSFRs(p.partNumber, regs.regsByAddr.values());

            // Decode Flash Word Size (Row Size on older dsPIC30F/PIC24FxxK devices)
            p.programBlockSize = FindAttribAsInt(doc, "edc:CodeMemTraits", "edc:wordsize", 0x4);
            if(p.coreType == CPUClass.mm)
                p.programBlockSize = 0x8;
            if((p.coreType == CPUClass.b) && (p.programBlockSize < 0x8))   // Probably unneeded
                p.programBlockSize = 0x8;
            if((p.coreType == CPUClass.e || p.coreType == CPUClass.c) && (p.programBlockSize < 0x4))   // Probably unneeded
                p.programBlockSize = 0x4;
            else if((p.coreType == CPUClass.f) && !p.partNumber.contains("K") && !p.partNumber.startsWith("dsPIC30"))    // A number of devices like PIC24FJ128GC010 have CodeMemTraits<wordsize> = 0x4, even though they are PIC24FJ/dsPIC33FJ parts with a programmable size of 0x2 instead.
            {
                if(!pic.regsByName.containsKey("NVMADR") && !pic.regsByName.containsKey("NVMADDR") && !pic.regsByName.containsKey("NVMADRL") && !pic.regsByName.containsKey("NVMADDRL"))
                    p.programBlockSize = 0x2;
            }
            if((p.coreType == CPUClass.f) && (p.partNumber.contains("K") || p.partNumber.startsWith("dsPIC30")))
                p.programBlockSize = 0x40;

            // Decode Flash Erase Page Size (Row Size on older dsPIC30F/PIC24FxxK devices)
            p.eraseBlockSize = (p.coreType == CPUClass.e) || (p.coreType == CPUClass.c) ? 1024
                               : p.coreType == CPUClass.mm ? 1024
                                 : 512;  // Default value in case if edc doesn't have "erasepagesize" defined
            p.eraseBlockSize = FindAttribAsInt(doc, "edc:Programming(edc:erasepagesize)", "edc:erasepagesize", p.eraseBlockSize) * (p.coreType == CPUClass.mm ? 0x4 : 0x2);
            if(FindNode(doc, "edc:PIC(edc:dosid==02073)") != null)      // SAAA = 0x800 (exception since older EDC databases missing this needed info)
                p.eraseBlockSize = 0x800;
            else if(FindNode(doc, "edc:PIC(edc:dosid==02786)") != null) // SABC = 0x800 (exception since older EDC databases missing this needed info)
                p.eraseBlockSize = 0x800;
            if((p.coreType == CPUClass.f) && (p.partNumber.contains("K") || p.partNumber.startsWith("dsPIC30")))   // dsPIC30F and PIC24FxxK devices
                p.eraseBlockSize = 0x40;

            p.devIDAddr = FindAttribAsLong(doc, "edc:DeviceIDSector", "edc:beginaddr", 0xFF0000L);
            p.devIDMask = ((p.coreType == CPUClass.c) || (p.coreType == CPUClass.e) || (p.coreType == CPUClass.f)) ? 0x00FFFFFF : 0x0FFFFFFF;
            p.devID = FindAttribAsLong(doc, "edc:DeviceIDSector", "edc:value", 0L);
            if((p.coreType == CPUClass.b) || (p.coreType == CPUClass.c) || (p.coreType == CPUClass.e) || (p.coreType == CPUClass.f))
                p.devID >>= 16;
            p.revIDAddr = ((p.coreType == CPUClass.c) || (p.coreType == CPUClass.e) || (p.coreType == CPUClass.f)) ? 0xFF0002 : p.devIDAddr;
            p.revIDMask = ((p.coreType == CPUClass.c) || (p.coreType == CPUClass.e) || (p.coreType == CPUClass.f)) ? 0x00FFFFFF : 0xF0000000;
            p.dsNum = FindAttribAsLong(doc, "edc:PIC", "edc:dsid", 0L);
            p.dosNum = FindAttribAsLong(doc, "edc:PIC", "edc:dosid", 0L);

            node = FindNode(doc, "edc:ProgramSubspace(edc:partitionmode==dual,edc:id==first)");
            boolean hasFBOOT = node != null;
            for(int partitionNum = 0; partitionNum < (hasFBOOT ? 3 : 1); partitionNum++)
            {
                configRegs = new DeviceRegs();
                if(partitionNum == 0)
                {
                    node = FindNode(doc, "edc:UserIDSector");   // FUID0
                    if(node != null)
                        configRegs.addAll(IndexSFRDCRNodes(node.getChildNodes()));

                    node = FindNode(doc, "edc:WORMHoleSector(edc:regionid==bootcfg)");      // FBOOT
                    if(node != null)
                        configRegs.addAll(IndexSFRDCRNodes(node.getChildNodes()));
                }

                node = FindNode(doc, "edc:AltConfigFuseSector");      // PIC32MM
                if(node != null)
                    configRegs.addAll(IndexSFRDCRNodes(node.getChildNodes()));
                node = FindNode(doc, "edc:ConfigFuseSector");
                if(node != null)
                    configRegs.addAll(IndexSFRDCRNodes(node.getChildNodes()));
                p.hasFlashConfigWords = (p.coreType == CPUClass.mm) || (node == null);
                if(p.hasFlashConfigWords)
                {
                    if(partitionNum == 0)
                    {
                        node = FindNode(doc, "edc:WORMHoleSector(edc:regionid==cfgmem)");
                        if(node == null)
                            node = FindNode(doc, "edc:WORMHoleSector");
                        if(node != null)
                            configRegs.addAll(IndexSFRDCRNodes(node.getChildNodes()));
                    }
                    else if(partitionNum == 1)
                    {
                        node = FindNode(doc, "edc:ProgramSubspace(edc:partitionmode==dual,edc:id==first)->edc:WORMHoleSector");
                        if(node != null)
                            configRegs.addAll(IndexSFRDCRNodes(node.getChildNodes()));
                    }
                    else if(partitionNum == 2)
                    {
                        node = FindNode(doc, "edc:ProgramSubspace(edc:partitionmode==dual,edc:id==second)->edc:WORMHoleSector");
                        if(node != null)
                            configRegs.addAll(IndexSFRDCRNodes(node.getChildNodes()));
                    }
                }

                for(Long i : configRegs.regsByAddr.keySet())
                {
                    sfr = configRegs.regsByAddr.get(i);
                    MemoryRegion cfg = new MemoryRegion();
                    cfg.name = sfr.name;
                    cfg.startAddr = sfr.addr;
                    cfg.endAddr = sfr.endAddr;
                    cfg.comment = sfr.desc;
                    cfg.partition = Partition.values()[partitionNum];
                    cfg.type = p.hasFlashConfigWords ? MemType.FLASHFUSE : MemType.BYTEFUSE;
                    cfg.eraseAlign = p.hasFlashConfigWords ? p.eraseBlockSize : (int)(cfg.endAddr - cfg.startAddr);
                    cfg.programAlign = p.hasFlashConfigWords ? p.programBlockSize : (int)(cfg.endAddr - cfg.startAddr);
                    devConfigDCRMemories.add(cfg);
                    devGLDMemories.add(cfg);
                    if(!p.hasFlashConfigWords)
                        devBootloadableRanges.add(cfg);
                }

                // Special Config Word bits (BACKBUG, SIGN, Code Protect)
                if((partitionNum == 0) && !configRegs.regsByAddr.isEmpty())
                {
                    TreeMap<String, BitField> backbugBits = configRegs.findBitfield("BACKBUG", null, null, null, null, 1, null, true);
                    if(backbugBits.isEmpty())
                        backbugBits = configRegs.findBitfield("BKBUG", null, null, null, null, 1, null, true);
                    if(backbugBits.isEmpty())
                        backbugBits = configRegs.findBitfield("DEBUG", "FICD", null, null, null, null, null, true);    // PIC32MM
                    if(backbugBits.isEmpty())
                        backbugBits = configRegs.findBitfield("DEBUG", null, null, null, null, 1, true, true);    // PIC24FJ128GA310 family
                    if(backbugBits.isEmpty())
                        backbugBits = configRegs.findBitfield(null, null, null, "Background Debug", null, 1, null, true);
                    if(backbugBits.isEmpty())
                        System.err.printf("ezbl_tools: Could not locate BACKBUG bit for %s (%d)\n", p.partNumber, partitionNum);
                    else if(backbugBits.size() > 1)
                    {
                        System.err.printf("ezbl_tools: Found %d config bits for BACKBUG\n", backbugBits.size());
                        for(BitField bf : backbugBits.values())
                        {
                            System.err.printf("            %s[%d] has '%s'\n", bf.parentSFR.name, bf.position, bf.name);
                        }
                    }
                    else
                    {
                        p.BACKBUGRegName = backbugBits.firstEntry().getValue().parentSFR.name;
                        p.BACKBUGAddr = backbugBits.firstEntry().getValue().parentSFR.addr;
                        p.BACKBUGPos = backbugBits.firstEntry().getValue().position;
                        p.BACKBUGMask = backbugBits.firstEntry().getValue().sfrBitmask;
                        if(p.hasFlashConfigWords)
                        {
                            long adjAddr = backbugBits.firstEntry().getValue().parentSFR.addr ^ ((long)(p.programBlockSize / 2));
                            EZBL_SFR adjReg = configRegs.regsByAddr.get(adjAddr);
                            if(adjReg != null)
                                p.BACKBUGAdjAffectedRegName = adjReg.name;
                        }
                    }

                    if(p.hasFlashConfigWords)
                    {
                        TreeMap<String, BitField> signBits = configRegs.findBitfield("SIGN", "FSIGN", null, null, null, 1, null, true);
                        if(signBits.isEmpty())
                            signBits = configRegs.findBitfield(null, "FSIGN", null, null, null, 1, true, true); // PIC24FJ256GB412 family has "Reserved" as the bit name, but does have FSIGN DCR
                        if(signBits.isEmpty())
                            signBits = configRegs.findBitfield("SIGN", "CONFIG1", null, null, null, 1, true, true); // PIC24FJ256GA110 family has two "SIGN" bits, the correct one in CONFIG1<15>, and a USB? reserved bit in CONFIG2<11>
                        if(signBits.isEmpty())
                            signBits = configRegs.findBitfield("SIGN", null, null, null, null, 1, true, true);
                        if(signBits.isEmpty())
                            signBits = configRegs.findBitfield("reserved", null, null, null, 15, 1, true, true);

                        if(signBits.isEmpty())
                            System.err.printf("ezbl_tools: Could not locate reserved bit for %s (%d)\n", p.partNumber, partitionNum);
                        else if(signBits.size() > 1)
                        {
                            System.err.printf("ezbl_tools: Found %d config bits for SIGN\n", signBits.size());
                            for(BitField bf : signBits.values())
                            {
                                System.err.printf("            %s[%d] has '%s'\n", bf.parentSFR.name, bf.position, bf.name);
                            }
                        }
                        else
                        {
                            p.ReservedBitRegName = signBits.firstEntry().getValue().parentSFR.name;
                            p.ReservedBitAddr = signBits.firstEntry().getValue().parentSFR.addr;
                            p.ReservedBitPos = signBits.firstEntry().getValue().position;
                            long adjAddr = signBits.firstEntry().getValue().parentSFR.addr ^ ((long)(p.programBlockSize / 2));
                            EZBL_SFR adjReg = configRegs.regsByAddr.get(adjAddr);
                            if(adjReg != null)
                                p.ReservedBitAdjAffectedRegName = adjReg.name;
                        }
                    }

                    TreeMap<String, BitField> cpBits = configRegs.findBitfield("BSS", null, null, null, null, null, null, true);
                    cpBits.putAll(configRegs.findBitfield("GSS", null, null, null, null, null, null, true));
                    cpBits.putAll(configRegs.findBitfield("CSS", null, null, null, null, null, null, true));
                    cpBits.putAll(configRegs.findBitfield("GCP", null, null, null, null, null, null, true));
                    cpBits.putAll(configRegs.findBitfield("CP", "FSEC", null, null, null, null, null, true));
                    if(cpBits.isEmpty())
                        System.err.printf("ezbl_tools: Could not locate Code Protection bits for %s (%d)\n", p.partNumber, partitionNum);
                    else
                    {
                        p.CodeProtectRegName = cpBits.firstEntry().getValue().parentSFR.name;
                        p.CodeProtectAddr = cpBits.firstEntry().getValue().parentSFR.addr;
                        p.CodeProtectMask = 0;
                        for(BitField bf : cpBits.values())
                        {
                            long maskForBF = 0;
                            for(int i = 0; i < bf.width; i++)
                            {
                                maskForBF = (maskForBF << 1) | 0x000001L;
                            }
                            p.CodeProtectMask |= maskForBF << bf.position;
                        }
                    }
                    db.insertSFRs(p.partNumber, configRegs.regsByAddr.values());
                }

                DecodedNodeList nodes = new DecodedNodeList();
                if(partitionNum == 0)
                {
                    node = FindNode(doc, "edc:DataSpace");
                    if(node != null)
                        nodes.putAll(IndexNodes(node.getChildNodes(), 1));
                    node = FindNode(doc, "edc:ExtendedDataSpace");
                    if(node != null)
                        nodes.put(node);
                    node = FindNode(doc, "edc:PhysicalSpace");
                    if(node != null)
                        nodes.putAll(IndexNodes(node.getChildNodes(), 0));
                    node = FindNode(doc, "edc:ProgramSpace");
                    if(node != null)
                        nodes.putAll(IndexNodes(node.getChildNodes(), 0));
                    node = FindNode(doc, "edc:ProgramSubspace(edc:partitionmode==single)");
                    if(node != null)
                        nodes.putAll(IndexNodes(node.getChildNodes(), 0));
                }
                else if(partitionNum == 1)
                {
                    node = FindNode(doc, "edc:ProgramSubspace(edc:partitionmode==dual,edc:id==first)");
                    if(node != null)
                        nodes.putAll(IndexNodes(node.getChildNodes(), 0));
                }
                else if(partitionNum == 2)
                {
                    node = FindNode(doc, "edc:ProgramSubspace(edc:partitionmode==dual,edc:id==second)");
                    if(node != null)
                        nodes.putAll(IndexNodes(node.getChildNodes(), 0));
                }
                if(nodes.list.size() == 0)
                {
                    Logger.getLogger(SQLitePartDatabase.class.getName()).log(Level.SEVERE, "Couldn't find edc:PhysicalSpace/edc:ProgramSpace/edc:ProgramSubSpace for " + p.partNumber + " (" + partitionNum + ")");
                    continue;
                }

                for(DecodedNode dn : nodes.list)
                {
                    if(dn.tagName.equals("edc:ProgramSubspace") || dn.tagName.equals("edc:EDSWindowSector"))
                        continue;

                    MemoryRegion mem = new MemoryRegion();
                    if(dn.attribs.containsKey("edc:beginaddr") && dn.attribs.containsKey("edc:endaddr"))
                    {
                        mem.startAddr = dn.getAttrAsLong("edc:beginaddr");
                        mem.endAddr = dn.getAttrAsLong("edc:endaddr");
                    }
                    else if(dn.attribs.containsKey("edc:xbeginaddr") && dn.attribs.containsKey("edc:xendaddr"))
                    {
                        mem.startAddr = dn.getAttrAsLong("edc:xbeginaddr");
                        mem.endAddr = dn.getAttrAsLong("edc:xendaddr");
                    }
                    else
                        continue;

                    mem.partition = Partition.values()[partitionNum];
                    mem.name = dn.getAttrAsString("edc:regionid");
                    if((mem.name == null) && dn.tagName.equals("edc:DataSpace"))
                        mem.name = "data";
                    if((mem.name == null) && dn.tagName.equals("edc:ExtendedDataSpace"))
                        mem.name = "edsdata";
                    if(dn.tagName.equals("edc:SFRDataSector"))
                        mem.type = MemType.SFR;
                    if(dn.tagName.equals("edc:DataSpace")
                       || dn.tagName.equals("edc:ExtendedDataSpace")
                       || dn.tagName.equals("edc:GPRDataSector"))
                        mem.type = MemType.RAM;
                    if(mem.name.equals("reset") || dn.tagName.equals("edc:ResetSector")
                       || mem.name.equals("ivt") || dn.tagName.equals("edc:VectorSector")
                       || mem.name.equals("aivt") || dn.tagName.equals("edc:AltVectorSector")
                       || mem.name.equals("program") || mem.name.equals("code") || dn.tagName.equals("edc:CodeSector")
                       || mem.name.equals("bootconfig") || dn.tagName.equals("edc:BootConfigSector") // PIC32MM regular bootflash
                       || mem.name.equals("auxflash") || dn.tagName.equals("edc:AuxCodeSector")
                       || mem.name.equals("auxvector") || dn.tagName.equals("edc:AuxVectorSector")
                       || mem.name.equals("auxreset") || dn.tagName.equals("edc:AuxResetSector"))
                    {
                        mem.type = MemType.ROM;
                        mem.eraseAlign = p.eraseBlockSize;
                        mem.programAlign = p.programBlockSize;
                    }
                    if((mem.name.equals("bootcfg") && dn.tagName.equals("edc:WORMHoleSector")) // FBOOT
                       || (mem.name.equals("cfgmem") && dn.tagName.equals("edc:WORMHoleSector"))
                       || (mem.name.equals("config") && dn.tagName.equals("edc:ConfigFuseSector")) // PIC32MM
                       || (mem.name.equals("altconfig") || dn.tagName.equals("edc:AltConfigFuseSector")))
                    {
                        mem.type = MemType.FLASHFUSE;
                        mem.eraseAlign = p.eraseBlockSize;
                        mem.programAlign = p.programBlockSize;
                    }
                    if((mem.name.equals("cfgmem") && dn.tagName.equals("edc:ConfigFuseSector"))
                       || mem.name.equals("userid") || dn.tagName.equals("edc:UserIDSector"))// Must be && because PIC32MM has "config" edc:ConfigFuseSector
                    {
                        mem.type = MemType.BYTEFUSE;
                        mem.eraseAlign = 0x2;
                        mem.programAlign = 0x2;
                    }
                    if(mem.name.equals("eedata") || dn.tagName.equals("edc:EEDataSector"))
                    {
                        mem.type = MemType.EEPROM;
                        mem.programAlign = 0x2;
                        mem.eraseAlign = p.eraseBlockSize;
                    }
                    if(dn.tagName.equals("edc:UserOTPSector"))
                    {
                        mem.type = MemType.OTP;
                        mem.programAlign = p.programBlockSize;
                    }
                    if(dn.tagName.equals("edc:DeviceIDSector")
                       || dn.tagName.equals("edc:EmulatorSector")
                       || dn.tagName.equals("edc:TestZone")
                       || dn.tagName.equals("edc:BACKBUGVectorSector")
                       || dn.tagName.equals("edc:ConfigWORMSector")
                       || dn.tagName.equals("edc:UniqueIDSector"))
                        mem.type = MemType.TEST;

                    devAllMemories.add(mem);

                    if((mem.type == MemType.DEBUG) || (mem.type == MemType.TEST))
                        continue;

                    if(mem.name.equals("auxvector") || mem.name.equals("auxreset"))
                        continue;

                    if((mem.type == MemType.ROM) || (mem.type == MemType.EEPROM))
                        devGLDMemories.add(mem);

                    // Don't care about these anymore
                    if(mem.name.equals("reset")
                       || mem.name.equals("ivt")
                       || mem.name.equals("_reserved")
                       || mem.name.equals("aivt")
                       || (mem.name.equals("cfgmem") && dn.tagName.equals("edc:WORMHoleSector"))
                       || (mem.name.equals("config") && dn.tagName.equals("edc:ConfigFuseSector"))
                       || (mem.name.equals("altconfig") && dn.tagName.equals("edc:AltConfigFuseSector")))
                        continue;

                    if((mem.type == MemType.ROM) || (mem.type == MemType.FLASHFUSE) || (mem.type == MemType.EEPROM) || (mem.type == MemType.OTP))   // OTP probably shouldn't be known to a bootloader
                    {
                        devBootloadableRanges.add(mem.clone());
                    }
                }
            }

            // Decode Interrupt List
            node = FindNode(doc, "edc:InterruptList");
            NodeList nodeList = node.getChildNodes();
            for(int i = 0, ivCount = 0; i < nodeList.getLength(); i++)
            {
                if(nodeList.item(i).getNodeType() != Document.ELEMENT_NODE)
                    continue;
                node = nodeList.item(i);
                InterruptVector iv = new InterruptVector();
                if((p.coreType != CPUClass.mm) && (p.coreType != CPUClass.other))
                {
                    iv.irqNum = Integer.decode(GetAttr(node, "edc:irq", String.valueOf(ivCount - 8)));
                    iv.vectorNum = iv.irqNum + 8;
                }
                else
                {
                    iv.irqNum = Integer.decode(GetAttr(node, "edc:irq", String.valueOf(ivCount)));
                    iv.vectorNum = iv.irqNum;
                }
                iv.trap = node.getNodeName().contains("edc:Trap");
                iv.name = GetAttr(node, "edc:cname", "_Interrupt" + String.valueOf(iv.vectorNum));
                iv.desc = GetAttr(node, "edc:desc", iv.name).replaceAll("Resereved", "Reserved");   // dsPIC33EP512MU810 __ReservedTrap7 has description of "Resereved", so fix this
                iv.implemented = !iv.name.startsWith("_Interrupt") && !iv.name.startsWith("Reserved") && !iv.desc.equals("Reserved") && !iv.name.startsWith("_Reserved");
                devInterrupts.add(iv);
                ivCount = iv.vectorNum + 1;
            }

            devBootloadableRanges = MemoryRegion.coalesce(devBootloadableRanges, true, true);   // Coalesce pad and sort the bootloader ranges

            db.insertDeviceProperties(p, true);
            db.insertMemories(MemoryTable.DeviceMemories, p.partNumber, devAllMemories);
            db.insertMemories(MemoryTable.GLDMemories, p.partNumber, devGLDMemories);
            db.insertMemories(MemoryTable.BootloadableRanges, p.partNumber, devBootloadableRanges);
            db.insertMemories(MemoryTable.DecodedConfigMemories, p.partNumber, devConfigDCRMemories);
            db.insertInterrupt(p.partNumber, devInterrupts);
            if(++partsProcessed % 32 == 0)
                db.saveDatabase(false);
        }

        System.out.printf("Saving output to '%s'\n", db.dbFilename);
        db.saveDatabase(true);

        return 0;
    }
}
