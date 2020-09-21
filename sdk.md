你的应用设置的 minSdkVersion 必需大于你使用库的 minSdkVersion ，比如例如有三个库，它们的 minSdkVersion 分别是 4, 7 和 9 ，那么你的 minSdkVersion 必需至少是 9 才能使用它们
在少数情况下，你仍然想用一个比你应用的 minSdkVersion 还高的库（处理所有的边缘情况，确保它只在较新的平台上使用），你可以使用 tools:overrideLibrary 标记，但请做彻底的测试！

targetSdkVersion 是 Android 系统提供前向兼容的主要手段。这是什么意思呢？随着 Android 系统的升级，某个 API 或者模块的行为可能会发生改变，但是为了保证APK 的行为还是和以前一致。只要 APK 的 targetSdkVersion 不变，即使这个 APK 安装在新 Android 系统上，其行为还是保持老的系统上的行为，这样就保证了系统对老应用的前向兼容性。