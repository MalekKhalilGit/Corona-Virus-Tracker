package io.javabrains.coronavirustracker.services;

import io.javabrains.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {
    private final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    private List<LocationStats> allStats = new ArrayList<>();

    @PostConstruct                          //performs methode after construction
    @Scheduled(cron = "* * 1 * * *")        //performs methode once a day
    public void fetchVirusData() throws IOException, InterruptedException {  //gets Data
        List<LocationStats> newStats = new ArrayList<>();       //makes a temp list that for refreshing
        HttpClient client = HttpClient.newHttpClient();         //makes a Http Client
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build(); //builds a Http Request
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());  //sends request with the client and return body as a String

        StringReader csvReader = new StringReader(httpResponse.body()); //CSV Reader translates csv
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader()
                .parse(csvReader);

        for (CSVRecord record : records) {          //builds a table though a loop
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));
            int latestCases = Integer.parseInt(record.get(record.size()-1));       //gets last column
            int preDayCases = Integer.parseInt(record.get(record.size()-2));       //gets second last column
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffFromPrevDay(latestCases - preDayCases);
            newStats.add(locationStat);
        }
        this.allStats = newStats;   //refreshes list
    }
}
