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

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;


/**
 *
 * @author C12128
 */
public class PICXMLLoader
{
    public static Document LoadPICXML(String baseSearchFolder, String fullPartNumber)
    {
        Document doc = null;
        InputStream picXML = null;
        List<String> searchFilenames = new ArrayList<String>();

        searchFilenames.add(fullPartNumber + ".PIC");
        searchFilenames.add("crownking.edc.jar");
        searchFilenames.add("*.jar");

        for(String searchFile : searchFilenames)
        {
            List<File> foundFiles = Multifunction.FindFiles(baseSearchFolder, searchFile, true);
            if(foundFiles.isEmpty())
                continue;
            Collections.sort(foundFiles);

            for(File f : foundFiles)
            {
                if(f.getName().equalsIgnoreCase(fullPartNumber + ".PIC"))
                {
                    doc = LoadPICXML(f);
                    if(doc != null)
                        return doc;
                }
                else if(f.getName().toLowerCase().endsWith(".jar") || f.getName().toLowerCase().endsWith(".zip"))
                {
                    try
                    {
                        Main.LoadRuntimeJar(f.getCanonicalPath());
                    }
                    catch(IOException ex)
                    {
                        Logger.getLogger(PICXMLLoader.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    picXML = ClassLoader.getSystemClassLoader().getResourceAsStream("content/edc/dsPIC30/" + fullPartNumber.toUpperCase() + ".PIC");
                    if(picXML != null)
                        break;
                    picXML = ClassLoader.getSystemClassLoader().getResourceAsStream("content/edc/dsPIC33/" + fullPartNumber.toUpperCase() + ".PIC");
                    if(picXML != null)
                        break;
                    picXML = ClassLoader.getSystemClassLoader().getResourceAsStream("content/edc/32xxxx/" + fullPartNumber.toUpperCase() + ".PIC");
                    if(picXML != null)
                        break;
                    picXML = ClassLoader.getSystemClassLoader().getResourceAsStream("content/edc/masksets/" + fullPartNumber.toUpperCase() + ".PIC");
                    if(picXML != null)
                        break;
                }
            }
            if(picXML != null)
                break;
        }
        if(picXML == null)
            return null;

        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(picXML);
            doc.getDocumentElement().normalize();
        }
        catch(ParserConfigurationException | SAXException | IOException ex)
        {
            Logger.getLogger(PICXMLLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doc;
    }

    public static Document LoadPICXML(File picFile)
    {
        Document doc = null;
        InputStream picXML = null;

        FileInputStream reader;
        try
        {
            reader = new FileInputStream(picFile);
            picXML = reader;
        }
        catch(FileNotFoundException ex)
        {
            Logger.getLogger(PICXMLLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(picXML == null)
            return null;

        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(picXML);
            doc.getDocumentElement().normalize();
        }
        catch(ParserConfigurationException | SAXException | IOException ex)
        {
            Logger.getLogger(PICXMLLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doc;
    }
}
