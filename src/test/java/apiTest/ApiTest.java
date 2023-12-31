package apiTest;

import api.EndPoints;
import api.dto.responseDto.AuthorDTO;
import api.dto.responseDto.PostDTO;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class ApiTest {
    final String USER_NAME = "autoapi";
    Logger logger = Logger.getLogger(getClass());

    @Test
    public void getPostsByUserTest(){
         PostDTO[] responseAsDto =   given()
                 .filter(new AllureRestAssured())
                    .contentType(ContentType.JSON)
                    .when()
                    .get(EndPoints.POST_BY_USER,USER_NAME)
                    .then()
                    .statusCode(200)
                    .log().all()
                    .extract()
                    .response().as(PostDTO[].class)
            ;
         logger.info("Number of posts = " + responseAsDto.length);
         logger.info("Tile post1 = " + responseAsDto[0].getTitle());
         logger.info("Username of post1 = " + responseAsDto[0].getAuthor().getUsername());

        for (int i = 0; i < responseAsDto.length; i++) {
            Assert.assertEquals("Username doesn't match", USER_NAME, responseAsDto[i].getAuthor().getUsername());
        }

        PostDTO[] expectedResult = {

                PostDTO.builder().title("test2").body("test body2").select1("All Users").uniquePost("no")
                        .author(AuthorDTO.builder().username("autoapi").build()).isVisitorOwner(false)
                        .build(),

                PostDTO.builder().title("test").body("test body").select1("All Users").uniquePost("no")
                        .author(AuthorDTO.builder().username("autoapi").build()).isVisitorOwner(false)
                        .build()
        };

        Assert.assertEquals("Numbers of posts doesn't match", expectedResult.length, responseAsDto.length);

        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(responseAsDto)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdDate", "author.avatar")
                .isEqualTo(expectedResult);

        softAssertions.assertAll();
    }


    @Test
    public void getAllPostsByUserNegative(){
        String actualResponse =
                given()
                        .filter(new AllureRestAssured())
                        .contentType(ContentType.JSON)
                        .log().all()
                        .when()
                        .get(EndPoints.POST_BY_USER, "notValidUser")
                        .then()
                        .statusCode(200)
                        .log().all()
                        .extract().response().getBody().asString();

        Assert.assertEquals("Message does not match", "Sorry, invalid user requested.undefined", actualResponse.replace("\"",""));
    }

    @Test
    public void getAllPostsByUserPath(){
        Response actualResponse =
                given()
                        .filter(new AllureRestAssured())
                        .contentType(ContentType.JSON)
                        .log().all()
                        .when()
                        .get(EndPoints.POST_BY_USER, USER_NAME)
                        .then()
                        .statusCode(200)
                        .log().all()
                        .extract().response();


        List<String> actualTitleList = actualResponse.jsonPath().getList("title", String.class);

        SoftAssertions softAssertions =  new SoftAssertions();
        for (int i = 0; i < actualTitleList.size(); i++) {
            softAssertions.assertThat(actualTitleList.get(i)).as("Item number " + i).contains("test");
        }

        List<Map> actualAuthorList = actualResponse.jsonPath().getList("author", Map.class);
        for (int i = 0; i < actualAuthorList.size(); i++) {
            softAssertions.assertThat(actualAuthorList.get(i).get("username"))
                    .as("Item number " + i).isEqualTo(USER_NAME);
        }

        softAssertions.assertAll();
    }

    @Test
    public void getAllPostsByUserSchema(){
        given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .log().all()
                .when()
                .get(EndPoints.POST_BY_USER, USER_NAME)
                .then()
                .statusCode(200)
                .log().all()
                .assertThat().body(matchesJsonSchemaInClasspath("response.json"));


    }

}
