# Android批量设备、批量apk安装，并自动登录验证

# 使用方法
```Bash
java -jar installer.jar
```

自动登录验证使用的UIAutomator脚本，需要自己写，写完编译成apk放到/data/apps/目录下。
要测试的apk安装包放在/data/output/目录下，数量不限。

设备是轮流安装，不是并行安装。
