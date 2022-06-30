import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.Select;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class WebScrapingTest extends BasicSetting {
    final static Logger logger = LogManager.getLogger(WebScrapingTest.class);
    private Properties conf = null;

    public static void main(String[] args) throws InterruptedException, IOException {
        Properties conf = PropertiesLoader.loadProperties();
        String accno = conf.getProperty("scraping.accno");
        String accpwd = conf.getProperty("scraping.accpwd");
        String acckey = conf.getProperty("scraping.acckey");

        //인터넷 익스플로러 웹드라이버 선언(import 부분에 Driver 선언부분 참고)
        File file = new File(WEB_DRIVER_PATH);
        System.setProperty(WEB_DRIVER_ID, file.getAbsolutePath());

        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        //빠른조회 페이지 접근(Full URL 입력 필요)
        driver.get("https://obank.kbstar.com/quics?page=C025255&cc=b028364:b028702");

        //계좌번호 입력(sendKeys 메소드 String에 조회하고자하는 계좌번호)
        WebElement loginID = driver.findElement(By.cssSelector("#account_num"));
        loginID.sendKeys("내계좌번호");

        //비밀번호 마우스로 입력 해제(간혹 기본설정이 가상키보드 사용으로 되어있는 경우 주석해제)
        /*WebElement PassWDCheck = driver.findElement(By.cssSelector(".rad_input > input[type=\"checkbox\"]"));
        PassWDCheck.click();*/

        //비밀번호 입력(format 메소드안 String을 조회계좌 4자리 비밀번호입력)
        WebElement PassWD = driver.findElement(By.cssSelector("#비밀번호"));
        PassWD.sendKeys("{}".format("계좌비밀번호"));

        //생년월일or사업자번호 유형으로 접근하도록 선택
        WebElement BirthDayClick = driver.findElement(By.cssSelector("#cond_user_num"));
        BirthDayClick.click();

        //생년월일or사업자번호 입력(sendKeys 메소드 String에 조회하고자하는 계좌번호)
        WebElement UserID = driver.findElement(By.cssSelector("#user_num"));
        UserID.sendKeys("생년월일or사업자번호");

        //특정기간 선택
        //==============================================================================================
        //시작년도
        WebElement StartToYear = driver.findElement(By.cssSelector("#조회시작년"));
        StartToYear.click();
        Select selectStartYear = new Select(StartToYear);
        selectStartYear.selectByVisibleText("시작년");

        //시작월
        WebElement StartToMonth = driver.findElement(By.cssSelector("#조회시작월"));
        StartToMonth.click();
        Select selectStartMonth = new Select(StartToMonth);
        selectStartMonth.selectByVisibleText("시작월");

        //시작일
        WebElement StartToDay = driver.findElement(By.cssSelector("#조회시작일"));
        StartToDay.click();
        Select selectStartDay = new Select(StartToDay);
        selectStartDay.selectByVisibleText("시작일");

        //끝년도
        WebElement EndToYear = driver.findElement(By.cssSelector("#조회끝년"));
        EndToYear.click();
        Select selectEndYear = new Select(EndToYear);
        selectEndYear.selectByVisibleText("끝년");

        //끝월
        WebElement EndToMonth = driver.findElement(By.cssSelector("#조회끝월"));
        EndToMonth.click();
        Select selectEndMonth = new Select(EndToMonth);
        selectEndMonth.selectByVisibleText("끝월");

        //끝일
        WebElement EndToDay = driver.findElement(By.cssSelector("#조회끝일"));
        EndToDay.click();
        Select selectEndDay = new Select(EndToDay);
        selectEndDay.selectByVisibleText("끝일");
        //==============================================================================================

        //조회버튼 클릭
        WebElement ClickToSubmit = driver.findElement(By.cssSelector("#pop_contents > div.btnArea > span > input[type=\"submit\"]"));
        ClickToSubmit.click();

        //데이터파싱(다음 버튼이 없을때까지 계속 파싱해주도록함)
        while(true) {
            String transDate = "";
            String transType = "";
            String Client = "";
            String OutAmt = "";
            String InAmt = "";
            String LastAmt = "";
            String cvname = "";
            int checkPoint = 0;

            WebElement ResultData = driver.findElement(By.cssSelector("#pop_contents > table.tType01 > tbody"));
            Document doc = Jsoup.parse("<html><head><body><table>"+ResultData.getAttribute("innerHTML")+"</table></body></head></html>");
            Elements tableElements = doc.select("table");
            Elements tableRowElements = tableElements.tagName("tr");
            for (int i = 0; i < tableRowElements.size(); i++) {
                Element row = tableRowElements.get(i);
                Elements rowItems = row.select("td");

                for (int j = 0; j < rowItems.size(); j++) {
                    checkPoint = (j+1)%9;

                    if(checkPoint==1) {
                        transDate = rowItems.get(j).text().trim();
                    }
                    else if(checkPoint==2) {
                        transType = rowItems.get(j).text().trim();
                    }
                    else if(checkPoint==4) {
                        OutAmt = rowItems.get(j).text().trim();
                    }
                    else if(checkPoint==5) {
                        InAmt = rowItems.get(j).text().trim();
                    }
                    else if(checkPoint==6) {
                        LastAmt = rowItems.get(j).text().trim();
                    }
                    else if(checkPoint==7) {
                        cvname = rowItems.get(j).text().trim();
                    }

                    if(checkPoint==0) {
                        Client = rowItems.get(j).text().trim();

                        logger.info("거래일자>>"+transDate);
                        logger.info("거래방식>>"+transType);
                        logger.info("출금액>>"+OutAmt);
                        logger.info("입금액>>"+InAmt);
                        logger.info("잔액>>"+LastAmt);
                        logger.info("거래점>>"+cvname);
                        logger.info("수취인/요청인>>"+Client);
                        logger.info("===============================================");
                    }
                }
            }

            if(driver.findElements(By.cssSelector("#pop_contents > div > div.leftArea > span.btn.small.icon > span.left.next > input[type=\"button\"]")).size()==0){
                break;
            }
            else {
                //다음페이지가 존재하면 클릭해서 다음페이지로 넘어가도록함
                WebElement ClickToNext = driver.findElement(By.cssSelector("#pop_contents > div > div.leftArea > span.btn.small.icon > span.left.next > input[type=\"button\"]"));
                ClickToNext.click();

                //다음페이지 출력 시간을 위해 3초정도 텀을 주는것이 안정적
                Thread.sleep(3000);
            }
        }
        //Close the browser
        driver.quit();
    }

}
