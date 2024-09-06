package com.ApisPaySkyTask;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ApiTest {

    private static ExtentReports extent;
    private static ExtentTest test;

    @DataProvider(name = "userData")
    public Object[][] userData() throws IOException {
        File file = new File("src/test/resources/data.json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

        StringBuilder jsonStringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonStringBuilder.append(line);
        }
        reader.close();

        JSONArray jsonArray = new JSONArray(jsonStringBuilder.toString());

        Object[][] data = new Object[jsonArray.length()][2];

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            data[i][0] = jsonObject.getString("name");
            data[i][1] = jsonObject.getString("job");
        }

        return data;
    }

    @Test(dataProvider = "userData")
    public void testUserCreation(String name, String job) {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter("extent-report.html");
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        test = extent.createTest("User Creation Test");

        String requestBody = String.format("{\"name\":\"%s\",\"job\":\"%s\"}", name, job);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody).log().all()
                .post("https://reqres.in/api/users");

        int statusCode = response.getStatusCode();
        System.out.println(statusCode);

        Assert.assertEquals(response.getStatusCode(), 201, "Status code is not 201");
        test.info("Response Code: " + statusCode);

        String responseBody = response.getBody().asString();
        Assert.assertTrue(responseBody.contains("id"), "Response does not contain 'id'");
        Assert.assertTrue(responseBody.contains(name), "Response does not contain the name");
        Assert.assertTrue(responseBody.contains(job), "Response does not contain the job");

        test.pass("User creation was successful with name: " + name + " and job: " + job);

        extent.flush();
    }
}
