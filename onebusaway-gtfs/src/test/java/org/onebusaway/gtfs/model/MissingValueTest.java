/**
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.gtfs.serialization.GtfsWriterTest;
import org.onebusaway.gtfs.serialization.mappings.InvalidStopTimeException;
import org.onebusaway.gtfs.services.MockGtfs;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

import static org.junit.Assert.*;

public class MissingValueTest {

    private MockGtfs _gtfs;

    private static DecimalFormat _format = new DecimalFormat("00", new DecimalFormatSymbols(Locale.ENGLISH));


    private File _tmpDirectory;

    @Before
    public void before() throws IOException {
        _gtfs = MockGtfs.create();

        //make temp directory for gtfs writing output
        _tmpDirectory = File.createTempFile("GtfsWriterMissingValueTest-", "-tmp");
        if (_tmpDirectory.exists())
            GtfsWriterTest.deleteFileRecursively(_tmpDirectory);
        _tmpDirectory.mkdirs();
    }

    @Test
    public void test() throws IOException {
        _gtfs.putMinimal();
        _gtfs.putDefaultTrips();
        _gtfs.putDefaultStops();
        _gtfs.putLines("stop_times.txt",
                "trip_id,stop_id,stop_sequence,arrival_time,departure_time,mean_duration_factor",
                "T10-0,100,0,,08:00:00,", "T10-0,200,1,05:55:55,09:00:00,09:00:00");

        GtfsRelationalDao dao = _gtfs.read();
        assertEquals(2, dao.getAllStopTimes().size());

        GtfsWriter writer = new GtfsWriter();
        System.out.println("outputlocation: " + _tmpDirectory );
        writer.setOutputLocation(_tmpDirectory);
        writer.run(dao);

        Scanner scan = new Scanner(new File(_tmpDirectory + "/stop_times.txt"));
        boolean foundUnderscoreParam = false;
        while(scan.hasNext()){
            String line = scan.nextLine();
            if(line.contains("end_pickup_dropoff_window")){
                foundUnderscoreParam = true;
            }
        }
        assertTrue("Column without was not found", foundUnderscoreParam);
    }

    @Test
    public void testPutMinimal() throws IOException {
        _gtfs.putMinimal();
        // Just make sure it parses without throwing an error.
        _gtfs.read();
    }

    @After
    public void teardown() {
        deleteFileRecursively(_tmpDirectory);
    }

    public static void deleteFileRecursively(File file) {

        if (!file.exists())
            return;

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files)
                    deleteFileRecursively(child);
            }
        }

        file.delete();
    }

}


