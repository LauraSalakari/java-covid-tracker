package demo.panda.covidtracker.controllers;

import demo.panda.covidtracker.models.LocationStats;
import demo.panda.covidtracker.services.CovidDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller // de
public class HomeController {

    @Autowired
    CovidDataService covidDataService; //get access to the data fetched in the service!

    @GetMapping("/") // map the root URL
    public String home(Model model){

        // the model allows us to pass info to the UI in Spring!
        // model.addAttribute("testName", "Panda"); // s is essentially the variable name, o the content

        // fetch and refactor allStats
        List<LocationStats> allStats = covidDataService.getAllStats();

        // sum up all the cases
        int totalReportedCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        // sum up all the deltas
        int changeInCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();

        model.addAttribute("locationStats", allStats);
        model.addAttribute("totalReportedCases", totalReportedCases);
        model.addAttribute("changeInCases", changeInCases);

        // calculate global total cases


        return "home"; // here I can return a template! this essentially maps to an existing html file!
        // if it were a REST controller, we would return in JSOn format!
    }
}
