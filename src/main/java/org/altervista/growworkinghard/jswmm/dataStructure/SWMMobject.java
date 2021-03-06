/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.altervista.growworkinghard.jswmm.dataStructure;

import org.altervista.growworkinghard.jswmm.dataStructure.formatData.readData.ReadDataFromFile;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.Conduit;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.OutsideSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.crossSections.Circular;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.crossSections.CrossSectionType;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.nodeObject.Junction;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.nodeObject.Outfall;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.rainData.GIUHsetup;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.rainData.RaingageSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.*;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.ReceiverRunoff.ReceiverRunoff;
import org.altervista.growworkinghard.jswmm.dataStructure.options.units.CubicMetersperSecond;
import org.altervista.growworkinghard.jswmm.dataStructure.options.units.ProjectUnits;
import org.altervista.growworkinghard.jswmm.dataStructure.options.time.GlobalTimeSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.options.time.TimeSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.routingDS.RoutingKinematicWaveSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.routingDS.RoutingSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.routingDS.RoutingSteadySetup;
import org.altervista.growworkinghard.jswmm.dataStructure.runoffDS.RunoffSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.runoffDS.SWMM5RunoffSetup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SWMMobject {

    private TimeSetup timeSetup;
    private RunoffSetup runoffSetup;
    private RoutingSetup routingSetup;
    private HashMap<String, RaingageSetup> raingageSetup = new HashMap<>();
    private HashMap<String, Area> areas = new HashMap<>();
    private Map<String, Junction> junctions = new ConcurrentHashMap<>();
    private HashMap<String, Outfall> outfalls = new HashMap<>();
    private Map<String, Conduit> conduit = new ConcurrentHashMap<>();
    private LinkedHashMap<Instant, Double> downstreamFlowRate;
    private ProjectUnits projectUnits;

    public SWMMobject(String inpFileName) {
        setTime();
        setRunoff();
        setRouting();
        setRaingages();
        setSubcatchments();
        setNodes();
        setLinks();
    }

    public SWMMobject() {
        setUnits();
        setTime();
        setRunoff();
        setRouting();
        setRaingages();
        setSubcatchments();
        setNodes();
        setLinks();
        setInitialValues(1);
        setInitialValues(2);
        setInitialValues(3);
        /*setInitialValues(4);
        setInitialValues(5);
        setInitialValues(6);
        setInitialValues(7);
        setInitialValues(8);
        setInitialValues(9);
        setInitialValues(10);
        setInitialValues(11);
        setInitialValues(12);
        setInitialValues(13);
        setInitialValues(14);
        setInitialValues(15);
        setInitialValues(16);
        setInitialValues(17);
        setInitialValues(18);
        setInitialValues(19);
        setInitialValues(20);
        setInitialValues(21);
        setInitialValues(22);
        setInitialValues(23);
        setInitialValues(24);
        setInitialValues(25);
        setInitialValues(26);
        setInitialValues(27);
        setInitialValues(28);
        setInitialValues(29);
        setInitialValues(30);*/

    }

    public TimeSetup getTimeSetup() {
        return timeSetup;
    }

    public RunoffSetup getRunoffSetup() {
        return runoffSetup;
    }

    public RoutingSetup getRoutingSetup() { return routingSetup; }

    public RaingageSetup getRaingage(String raingageName) {
        return raingageSetup.get(raingageName);
    }

    public Area getAreas(String areaName) {
        return areas.get(areaName);
    }

    public Conduit getConduit(String conduitName) {
        return conduit.get(conduitName);
    }


    private void setUnits() {
        String units = "CMS";

        if (units == "CMS") {
            this.projectUnits = new CubicMetersperSecond();
        }
    }

    private void setTime() {
        Instant startDate = Instant.parse("2018-01-01T00:00:00Z");
        Instant endDate = Instant.parse("2018-01-01T02:00:00Z");
        Instant reportStartDate = Instant.parse("2018-01-01T00:00:00Z");
        Instant reportEndDate = Instant.parse("2018-01-01T02:00:00Z");
        Instant sweepStart = Instant.parse("2018-01-01T00:00:00Z");
        Instant sweepEnd = Instant.parse("2018-01-01T00:00:00Z");
        Integer dryDays = 0;

        this.timeSetup = new GlobalTimeSetup(startDate, endDate, reportStartDate, reportEndDate,
                sweepStart, sweepEnd, dryDays);
    }

    private void setRunoff() {
        Long runoffStepSize = 60L; //must be in seconds!!

        Double minimumStepSize = 1.0e-8;
        Double maximumStepSize = 1.0e+3;
        Double absoluteRunoffTolerance = 1.0e-5;
        Double relativeRunoffTolerance = 1.0e-5;

        Instant initialTime = timeSetup.getStartDate();
        Instant totalTime = timeSetup.getEndDate();

        this.runoffSetup = new SWMM5RunoffSetup(initialTime, totalTime, runoffStepSize,
                minimumStepSize, maximumStepSize, absoluteRunoffTolerance, relativeRunoffTolerance, projectUnits);
    }

    private void setRouting() {
        Long routingStepSize = 30L;
        Double toleranceMethod = 0.0015;

        //TODO need change to parallelize
        //routingSetup = new RoutingKinematicWaveSetup(routingStepSize, toleranceMethod);
        routingSetup = new RoutingSteadySetup(routingStepSize);
    }

    private void setRaingages() {

        //for (each raingage)


        ReadDataFromFile readDataFromFile = null;
        /*TODO check if a and n or data
        try {
            readDataFromFile = new ReadSWMM5RainfallFile("./data/rainfallNetwork.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //ProjectUnits raingageUnits = new CubicMetersperSecond();
        String raingageName = "RG1";
        String dataSourceName = "rainfallNetwork.txt";
        String stationName = "RG1";
        Long rainfallStepSize = 60L;
        //TODO FORMATDATA
        //Instant rainfallStartDate = Instant.parse("2000-04-04T00:00Z");
        //Instant rainfallEndDate = Instant.parse("2000-04-04T00:00Z");
        //Double snowpack = 0.0;

        raingageSetup.put(raingageName, new GIUHsetup(rainfallStepSize));
    }

    private void setSubcatchments() {
        //for (each subcatchment)
        setAreas("1", 1.937);
        setAreas("2", 1.731);
        setAreas("3", 0.481);
        setAreas("4", 0.547);
        setAreas("5", 2.141);
        setAreas("6", 0.383);
        setAreas("7", 0.353);
        setAreas("8", 0.999);
        setAreas("9", 1.583);
        //setAreas("10", 1.583);
        setAreas("10", 0.633);
    }

    private void setAreas(String areaName, double subcatchmentArea) {
        //ReadDataFromFile subcatchmentReadDataFromFile = new ReadSWMM5RainfallFile("ciao");
        //AcquiferSetup acquiferSetup = new Acquifer();
        //SnowPackSetup subcatchmentSnowpack = new SnowPack();
        //ProjectUnits subcatchmentUnits = new CubicMetersperSecond();
        //String subcatchmentName = "Sub1";

        Double imperviousPercentage = 0.25;
        Double imperviousWOstoragePercentage = 0.25;

        Double depressionStorageImpervious = 0.00005;
        Double depressionStoragePervious = 0.00005;

        String perviousTo = "OUTLET";
        Double percentageFromPervious = 0.0;

        String imperviousTo = "OUTLET";
        Double percentageFromImpervious = 0.0;

        Double roughnessCoefficientPervious = 0.1;
        Double roughnessCoefficientImpervious = 0.01;

        Double characteristicWidth = 100.0;
        Double areaSlope = 0.01;
        Double curbLength = 0.0;

        String raingageName = "RG1";
        ReceiverRunoff receiverSubcatchment = null;

        List<Subarea> subareas = divideAreas(imperviousPercentage, subcatchmentArea,
                imperviousWOstoragePercentage, depressionStoragePervious, depressionStorageImpervious,
                roughnessCoefficientPervious, roughnessCoefficientImpervious,
                perviousTo, imperviousTo, percentageFromPervious, percentageFromImpervious);

        areas.put(areaName, new Area(subcatchmentArea, raingageSetup.get(areaName),
                characteristicWidth, areaSlope, subareas));
    }

    private void setNodes() {
        setJunctions("J1", 0.0);
        setJunctions("J2", 0.0);
        setJunctions("J3", 0.0);
        setJunctions("J4", 0.0);
        setJunctions("J5", 0.0);
        setJunctions("J6", 0.0);
        setJunctions("J7", 0.0);
        setJunctions("J8", 0.0);
        setJunctions("J9", 0.0);
        setJunctions("J10", 0.0);
        setJunctions("J11", 0.0);
        setOutfalls();
    }

    private void setJunctions(String nodeName, double nodeElevation) {
        //for (each junction)
        //ReadDataFromFile junctionReadDataFromFile = new ReadSWMM5RainfallFile("ciao");
        //WriteDataToFile writeDataToFile = new WriteSWMM5RainfallToFile();
        //ExternalInflow dryWeatherInflow = new DryWeatherInflow();
        //ExternalInflow RDII = new RainfallDependentInfiltrationInflow();
        //ProjectUnits nodeUnits = new CubicMetersperSecond();

        Double maximumDepthNode = 3.0;
        Double initialDepthNode = 0.0;
        Double maximumDepthSurcharge = 1.0;
        Double nodePondingArea = 200.0;

        junctions.put(nodeName, new Junction(nodeElevation, maximumDepthNode, initialDepthNode,
                maximumDepthSurcharge, nodePondingArea));
    }

    private void setOutfalls() {
        //for (each outfall)
        //ReadDataFromFile outfallReadDataFromFile = new ReadSWMM5RainfallFile("ciao");
        //WriteDataToFile outfallWriteDataToFile = new WriteSWMM5RainfallToFile();
        //ExternalInflow outfallDryWeatherInflow = new DryWeatherInflow();
        //ExternalInflow outfallRDII = new RainfallDependentInfiltrationInflow();
        //ProjectUnits outfallNodeUnits = new CubicMetersperSecond();
//        String nodeName = "Out1";
//        Double nodeElevation = 0.0;
//        Double fixedStage = 0.0;
//        LinkedHashMap<Instant, Double> tidalCurve = null;
//        LinkedHashMap<Instant, Double> stageTimeseries = null;
//        boolean gated = false;
//        String routeTo = "";
//
//        outfalls.put(nodeName, new Outfall(nodeElevation, fixedStage, tidalCurve,stageTimeseries,
//                gated, routeTo));
    }

    private void setLinks() {
        //for (each link) TODO check if present
        setConduit("11", 120.0,"J1", -239.0, 197.0, 0.0,
                "J3", -119.0, 197.0, 0);
        setConduit("12", 122,  "J2",-119.0, 319.0, 0.0,
                "J3",-119.0, 197.0, 0);
        setConduit("13", 119,  "J3",-119.0, 197.0, 0.0,
                "J4",0.0, 197.0, 0);
        setConduit("14", 43,   "J5",111.0, 240.0, 0.0,
                "J7",111.0, 197.0, 0);
        setConduit("15", 92,   "J6",203.0, 197.0, 0.0,
                "J7",111.0, 197.0, 0);
        setConduit("16", 111,   "J7",111.0, 197.0, 0.0,
                "J4",0.0, 197.0, 0);
        setConduit("17", 81,   "J4",  0.0, 197.0, 0.0,
                "J8",0.0, 116.0, 0);
        setConduit("18", 150,   "J9",150.0, 116.0, 0.0,
                "J8",0.0, 116.0, 0);
        setConduit("19", 134,  "J10",-134.0, 116.0, 0.0,
                "J8",0.0, 116.0, 0);
        setConduit("20", 116,   "J8",  0.0, 116.0, 0.0,
                "J11",0.0,   0.0, 0);
    }

    private void setConduit(String linkName, double linkLength, String upName, double upX, double upY, double upZ,
                            String downName, double downX, double downY, double downZ) {

        Double linkRoughness = 120.0; //Gs coefficient
        Double upstreamOffset = 0.0;
        Double downstreamOffset = 0.0;
        //Double initialFlowRate = 0.0;
        Double fillCoeff = 0.9; // Max flowrate
        Double diameter = 0.5;

        CrossSectionType crossSectionType = new Circular(diameter);
        //ProjectUnits linkUnits = new CubicMetersperSecond();

        OutsideSetup upstreamOutside = new OutsideSetup(upName, upstreamOffset,
                fillCoeff, upX, upY, upZ);
        OutsideSetup downstreamOutside = new OutsideSetup(downName, downstreamOffset,
                fillCoeff, downX, downY, downZ);

        conduit.put(linkName, new Conduit(routingSetup, crossSectionType, upstreamOutside, downstreamOutside,
                linkLength, linkRoughness));
    }

    private List<Subarea> divideAreas(Double imperviousPercentage, Double subcatchmentArea,
                                      Double imperviousWOstoragePercentage, Double depressionStoragePervious, Double depressionStorageImpervious,
                                      Double roughnessCoefficientPervious, Double roughnessCoefficientImpervious,
                                      String perviousTo, String imperviousTo, Double percentageFromPervious, Double percentageFromImpervious) {

        Double imperviousWOStorageArea = subcatchmentArea * imperviousPercentage * imperviousWOstoragePercentage;
        Double imperviousWStorageArea = subcatchmentArea * imperviousPercentage  - imperviousWOStorageArea;
        Double perviousArea = subcatchmentArea * (1-imperviousPercentage);

        List<Subarea> tmpSubareas = new LinkedList<>();
        if(imperviousPercentage == 0.0) {
            tmpSubareas.add(new Pervious(perviousArea, depressionStoragePervious,
                    roughnessCoefficientImpervious, projectUnits));
        }
        else if(imperviousPercentage == 1.0) {
            if (imperviousWOstoragePercentage != 0.0) {
                tmpSubareas.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious, projectUnits));
            }
            if (imperviousWOstoragePercentage != 1.0) {
                tmpSubareas.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious, projectUnits));
            }

        }
        else {
            if (perviousTo.equals("IMPERVIOUS")) {
                tmpSubareas.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious, projectUnits));

                List<Subarea> tmpConnections = null;
                tmpConnections.add(new Pervious(perviousArea, depressionStoragePervious,
                        roughnessCoefficientPervious, projectUnits));

                tmpSubareas.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious, percentageFromPervious,
                        tmpConnections, projectUnits));
            }
            else if(perviousTo.equals("OUTLET")) {
                tmpSubareas.add(new Pervious(perviousArea, depressionStoragePervious,
                        roughnessCoefficientPervious, projectUnits));
            }

            if (imperviousTo.equals("PERVIOUS")) {

                List<Subarea> tmpConnections = null;
                tmpConnections.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious, projectUnits));
                tmpConnections.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious, percentageFromPervious,
                        tmpConnections, projectUnits));

                tmpSubareas.add(new Pervious(perviousArea, depressionStoragePervious, roughnessCoefficientPervious,
                        percentageFromImpervious, tmpConnections, projectUnits));
            }
            else if (imperviousTo.equals("OUTLET")) {
                tmpSubareas.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious, projectUnits));
                tmpSubareas.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious, projectUnits));
            }
        }
        return tmpSubareas;
    }

    //TODO add at each subcatchment!
    private void setInitialValues(Integer id) {
        setSubareasInitialValue(id, "1");
        setSubareasInitialValue(id, "2");
        setSubareasInitialValue(id, "3");
        setSubareasInitialValue(id, "4");
        setSubareasInitialValue(id, "5");
        setSubareasInitialValue(id, "6");
        setSubareasInitialValue(id, "7");
        setSubareasInitialValue(id, "8");
        setSubareasInitialValue(id, "9");
        setSubareasInitialValue(id, "10");
        setInitialTime(id, "11");
        setInitialTime(id, "12");
        setInitialTime(id, "13");
        setInitialTime(id, "14");
        setInitialTime(id, "15");
        setInitialTime(id, "16");
        setInitialTime(id, "17");
        setInitialTime(id, "18");
        setInitialTime(id, "19");
        setInitialTime(id, "20");
    }

    private void setSubareasInitialValue(Integer id, String areaName) {
        for( Subarea subarea : areas.get(areaName).getSubareas() ) {
            subarea.setAreaFlowRate(id, timeSetup.getStartDate(), 0.0);
            subarea.setRunoffDepth(id, timeSetup.getStartDate(), 0.0);
            subarea.setTotalDepth(id, timeSetup.getStartDate(), 0.0);
        }
    }

    private void setInitialTime(Integer id, String linkName) {
        Instant time = timeSetup.getStartDate();
        while (time.isBefore(timeSetup.getEndDate())) {
            conduit.get(linkName).getUpstreamOutside().setStreamFlowRate(id, time, 0.01);
            conduit.get(linkName).getUpstreamOutside().setStreamFlowRate(id, time, 0.01);
            time = time.plusSeconds(routingSetup.getRoutingStepSize());
        }
        conduit.get(linkName).getUpstreamOutside().setStreamFlowRate(id, time, 0.01);
        conduit.get(linkName).getUpstreamOutside().setStreamFlowRate(id, time, 0.01);
    }

    public List<Double> readFileList(String fileName) {
        String line;

        List<Double> testingValues = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                testingValues.add(Double.parseDouble(line));
            }

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }

        return testingValues;
    }

    public LinkedHashMap<Instant, Double> adaptDataSeries(Long toStepSize, Long fromStepSize, Long finalTime,
                                                           Long initialTime, LinkedHashMap<Instant, Double> HMData) {

        LinkedHashMap<Instant, Double> adaptedData = new LinkedHashMap<>();
        Long currentDataTime = initialTime;

        for (Long currentTime = initialTime; currentTime<finalTime; currentTime+=toStepSize) {

            while(currentDataTime <= currentTime) {
                currentDataTime += fromStepSize;
            }

            Long upperTime = currentDataTime;
            Double upperRainfallData = 0.0;
            if(HMData.get(Instant.ofEpochSecond(upperTime)) != null) {
                upperRainfallData = HMData.get(Instant.ofEpochSecond(upperTime));
            }

            Long lowerTime = upperTime - fromStepSize;
            Double lowerData = 0.0;
            if(HMData.get(Instant.ofEpochSecond(lowerTime)) != null) {
                lowerData = HMData.get(Instant.ofEpochSecond(lowerTime));
            }

            Double currentData = interpolateData(currentTime, lowerTime, lowerData,
                    upperTime, upperRainfallData);

            adaptedData.put(Instant.ofEpochSecond(currentTime), currentData);
        }
        adaptedData.put(Instant.ofEpochSecond(finalTime), HMData.get(Instant.ofEpochSecond(finalTime)));

        //System.out.println(adaptedRainfallData);
        return adaptedData;
    }

    private Double interpolateData(Long currentRunoffTime, Long lowerTime,
                                   Double lowerTimeData, Long upperTime, Double upperTimeData) {
        Long rangeTime = upperTime - lowerTime;

        if( rangeTime == 0 ) { return lowerTimeData; }
        else {
            if (upperTimeData == null) {
                upperTimeData = 0.0;
            }
            if (lowerTimeData == null) {
                lowerTimeData = 0.0;
            }

            Double numerator = upperTimeData - lowerTimeData;

            return lowerTimeData + numerator / rangeTime * (currentRunoffTime - lowerTime);
        }
    }

    public void setNodeFlowRate(String nodeName, HashMap<Integer, LinkedHashMap<Instant, Double>> flowRate) {
        junctions.get(nodeName).sumFlowRate(flowRate);
    }

    public void setLinkFlowRate(String linkName, HashMap<Integer, LinkedHashMap<Instant, Double>> flowRate) {
        conduit.get(linkName).getUpstreamOutside().sumStreamFlowRate(flowRate);
    }

    public void upgradeSubtrees(String outLink, HashMap<Integer, List<Integer>> subtrees) {

        //System.out.println("Part 1");

        double downstreamDepthOut = getConduit(outLink).getUpstreamOutside().getWaterDepth();
        double maxDepth = downstreamDepthOut;
        Integer maxId = Integer.parseInt(outLink);

        for (Integer subtreeId : subtrees.keySet()) {
            if (getConduit(String.valueOf(subtreeId)) != null) {
                double downstreamDepth = getConduit(String.valueOf(subtreeId)).getDownstreamOutside().getWaterDepth();
                if (downstreamDepth > maxDepth) {
                    maxDepth = downstreamDepth;
                    maxId = subtreeId;
                }
            }
        }

        //System.out.println("Part 2");

        if (maxId != Integer.parseInt(outLink)) {
            upgradeStream(outLink, downstreamDepthOut - maxDepth);
        }

        //System.out.println("Part 3");

        for (List<Integer> subtreeList : subtrees.values()) {

            String firstSon = String.valueOf(subtreeList.get(subtreeList.size() - 1));

            //System.out.println("firstSon " + firstSon);

            if (getConduit(firstSon) != null) {
                double downstreamDepth = getConduit(firstSon).getDownstreamOutside().getWaterDepth();
                if (downstreamDepth - maxDepth != 0.0) {

                    //System.out.println("subtreeList " + subtreeList );
                    //System.out.println("downstreamDepth - maxDepth " + (downstreamDepth - maxDepth) );

                    upgradeStream(subtreeList, downstreamDepth - maxDepth);
                }
            }

            //System.out.println("END part 3");
        }
    }

    private void upgradeStream(List<Integer> subtreeList, double delta) {
        for (Integer subtreeLink : subtreeList) {
            String currentLink = String.valueOf(subtreeLink);

            //System.out.println("Conduit " + subtreeLink);

            upgradeStream(currentLink, delta);

            //System.out.println("For loop upgradeStream");
        }

        //System.out.println("END For loop upgradeStream");

    }

    private void upgradeStream(String currentLink, double delta) {

            if (getConduit(currentLink) != null) {
                OutsideSetup upstream = getConduit(currentLink).getUpstreamOutside();
                OutsideSetup downstream = getConduit(currentLink).getDownstreamOutside();

                //System.out.println("upstream " + upstream );
                upstream.upgradeOffset(delta);

                //System.out.println("downstream " + downstream );
                //System.out.println("delta " + delta );
                downstream.upgradeOffset(delta);

                //System.out.println("END UPSTREAM upgrade!");
            }
    }
}