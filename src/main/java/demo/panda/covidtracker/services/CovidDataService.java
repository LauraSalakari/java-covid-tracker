package demo.panda.covidtracker.services;

import demo.panda.covidtracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service //mark this class as a Spring service
public class CovidDataService {

    private static String COVID_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    //create a getter for the data so we can use it in the controller to pass to the UI
    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct //when the construction of this service is complete, execute this method
    @Scheduled(cron = "* * 1 * * *") // schedule to run the method on the first hour of every day
    public void fetchCovidData() throws IOException, InterruptedException {
        // exceptions are part of the method signature to handle if client.send() fails

        // create a new variable for the newly fetched data so that there is always data in the main variable!
        // CONCURRENCY: avoid errors while the app is constructing the allStats list!
        List<LocationStats> newStats = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(COVID_DATA_URL))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        //System.out.println(httpResponse.body()); // print out the fetched data from the github url that was turned to a URI

        // Create a string reader to read the CSV file received
        StringReader csvBodyReader = new StringReader(httpResponse.body());

        // once the data is fetched, parse it with Commons CSV -  dependency added!!
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);



        // loop through records to get what we need
        for (CSVRecord record : records) {
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State")); // extract the Province/State column
            locationStat.setCountry(record.get("Country/Region")); // extract the Country/Region column

            // get the latest cases by finding the last index of the headers and fetching data in that column:
            int latestCases = Integer.parseInt(record.get(record.size() - 1));

            // find the previous day cases
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));

            // set latest cases
            locationStat.setLatestTotalCases(latestCases);
            // set cases delta
            locationStat.setDiffFromPrevDay(latestCases - prevDayCases);

            // System.out.println(locationStat);

            // push to temp list
            newStats.add(locationStat);
        }

        // once the data is fetched and parsed, set it to the allStats variable that will be rendered
        this.allStats = newStats;
    }

}

//https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv

