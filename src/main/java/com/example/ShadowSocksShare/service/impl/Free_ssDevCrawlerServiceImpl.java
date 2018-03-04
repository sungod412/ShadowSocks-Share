package com.example.ShadowSocksShare.service.impl;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * https://free-ss.site/
 */
@Slf4j
@Profile("dev")
@Service("free_ssCrawlerServiceImpl")
public class Free_ssDevCrawlerServiceImpl extends ShadowSocksCrawlerService {
	// 目标网站 URL
	private static final String TARGET_URL = "https://free-ss.site/";
	// userAgent
	private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36";
	// 访问目标网站，是否启动代理
	@Value("${proxy.enable}")
	@Getter
	private boolean proxyEnable;
	// 代理地址
	@Getter
	@Value("${proxy.host}")
	private String proxyHost;
	// 代理端口
	@Getter
	@Value("${proxy.port}")
	private int proxyPort;
	@Value("${proxy.free-ss.enable}")
	private boolean ssProxyEnable;
	@Value("${proxy.free-ss.host}")
	private String ssProxyHost;
	@Value("${proxy.free-ss.port}")
	private int ssProxyPort;
	@Value("${proxy.free-ss.socks}")
	private boolean ssSocks;

	@Value("${phantomjs.path}")
	private String phantomjsPath;

	@Autowired
	private ResourceLoader resourceLoader;

	public ShadowSocksEntity getShadowSocks() {
		// WebDriver driver = new RemoteWebDriver(new URL(serverUrl), capability);
		WebDriver driver = null;
		try {
			// 设置必要参数
			DesiredCapabilities capability = DesiredCapabilities.chrome();

			// 设置代理
			/*if (true) {
				String proxyServer = "49.65.167.129" + ":" + 1080;
				Proxy proxy = new Proxy();
				// proxy.setAutodetect(false).setProxyType(Proxy.ProxyType.SYSTEM);
				if (true) {
					proxy.setSocksProxy(proxyServer);
					proxy.setSocksVersion(5);
				} else {
					proxy.setHttpProxy(proxyServer).setFtpProxy(proxyServer).setSslProxy(proxyServer);
				}
				capability.setCapability(CapabilityType.PROXY, proxy);
			}*/


			// 参数配置：http://phantomjs.org/api/webpage/property/settings.html
			capability.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "javascriptEnabled", true);
			capability.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "loadImages", true);
			capability.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", userAgent); // userAgent
			capability.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "webSecurityEnabled", false);

			// SSL 证书支持
			capability.setCapability("acceptSslCerts", true);
			// 截屏支持
			// capability.setCapability("takesScreenshot", false);
			// CSS 搜索支持
			capability.setCapability("cssSelectorsEnabled", true);
			// JS 支持
			capability.setJavascriptEnabled(true);
			capability.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, resourceLoader.getResource(phantomjsPath).getFile().getAbsolutePath());

			// 命令行选项：http://phantomjs.org/api/command-line.html
			List<String> cliArgsCap = new ArrayList<>();
			/*cliArgsCap.add("--proxy=49.65.167.129:1080");
			cliArgsCap.add("--proxy-type=socks5");*/
			cliArgsCap.add("--ssl-protocol=any");
			cliArgsCap.add("--script-encoding=utf8");
			cliArgsCap.add("--webdriver-logfile=logs/phantomjsdriver.log");
			cliArgsCap.add("--webdriver-loglevel=INFO");
			cliArgsCap.add("--debug=false");
			capability.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);

			driver = new PhantomJSDriver(capability);
			driver.manage().window().maximize();
			// driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
			// driver.manage().timeouts().pageLoadTimeout(TIME_OUT, TimeUnit.SECONDS);

			driver.get(TARGET_URL);

			TimeUnit.SECONDS.sleep(10);

			if (true) {
				List<WebElement> divList = driver.findElements(By.xpath("//div[contains(@class, 'dataTables_wrapper')]"));
				for (WebElement dev : divList) {
					// log.debug("height =================>{}", dev.getSize().height);
					// log.debug("isDisplayed =================>{}", dev.isDisplayed());
					// log.debug("DIV innerHTML =================>{}", dev.getAttribute("innerHTML"));

					if (dev.isDisplayed()) {
						List<WebElement> trList = dev.findElements(By.xpath("./table/tbody/tr"));

						Set<ShadowSocksDetailsEntity> set = new HashSet<>(trList.size());
						for (WebElement tr : trList) {
							// log.debug("TR innerHTML =================>{}", tr.getAttribute("innerHTML"));

							String server = tr.findElement(By.xpath("./td[2]")).getText();
							String server_port = tr.findElement(By.xpath("./td[3]")).getText();
							String password = tr.findElement(By.xpath("./td[4]")).getText();
							String method = tr.findElement(By.xpath("./td[5]")).getText();

							if (StringUtils.isNotBlank(server) && StringUtils.isNumeric(server_port) && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(method)) {
								ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(server, Integer.parseInt(server_port), password, method, SS_PROTOCOL, SS_OBFS);
								// 该网站账号默认为可用，不在此验证可用性
								ss.setValid(true);
								ss.setValidTime(new Date());
								ss.setTitle("免费上网账号");
								ss.setRemarks("https://free-ss.site/");
								ss.setGroup("ShadowSocks-Share");

								// 测试网络
								if (isReachable(ss))
									ss.setValid(true);

								// 无论是否可用都入库
								set.add(ss);

								log.debug("*************** 第 {} 条 ***************{}{}", set.size(), System.lineSeparator(), ss);
							}
						}

						// 3. 生成 ShadowSocksEntity
						ShadowSocksEntity entity = new ShadowSocksEntity(TARGET_URL, driver.getTitle(), true, new Date());
						entity.setShadowSocksSet(set);
						return entity;
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (driver != null) {
				driver.quit();
				// driver.close();
			}
		}
		return new ShadowSocksEntity(TARGET_URL, "免费上网账号", false, new Date());
	}

	/**
	 * 网页内容解析 ss 信息
	 */
	@Override
	protected Set<ShadowSocksDetailsEntity> parse(Document document) {
		return null;
	}

	/**
	 * 目标网站 URL
	 */
	@Override
	protected String getTargetURL() {
		return TARGET_URL;
	}

	/*public boolean waitForAjax(WebDriver driver) {
		WebDriverWait wait = new WebDriverWait(driver, 30, 500);
		ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return ((Long) ((JavascriptExecutor) driver).executeScript("return jQuery.active") == 0);
				} catch (Exception e) {
					return true;
				}
			}
		};
		ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState")
						.toString().equals("complete");
			}
		};
		return wait.until(jQueryLoad) && wait.until(jsLoad);
	}*/
}
