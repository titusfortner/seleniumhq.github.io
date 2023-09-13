from selenium import webdriver


def test_phone(log):
    driver = webdriver.Chrome()
    set_device_metrics_override = dict({
        "width": 375,
        "height": 812,
        "deviceScaleFactor": 50,
        "mobile": True
    })
    driver.execute_cdp_cmd('Emulation.setDeviceMetricsOverride', set_device_metrics_override)
    driver.get("https://www.selenium.dev")
    driver.quit()


def test_phone(log):
    driver = webdriver.Chrome()
    set_device_metrics_override = dict({
        "width": 375,
        "height": 812,
        "deviceScaleFactor": 50,
        "mobile": True
    })
    driver.execute_cdp_cmd('Emulation.setDeviceMetricsOverride', set_device_metrics_override)
    driver.get("https://www.selenium.dev")
    driver.quit()

