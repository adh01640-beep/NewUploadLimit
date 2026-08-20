plugins {
    id("com.aliucord.gradle")
}

aliucord {
    author("Adham")
    description("Updates the free upload limit to 20MB and adapts client-side attachment checks.")
    version("1.0.0")
    buildUrl("https://raw.githubusercontent.com/adh01640-beep/plugins/builds/NewUploadLimit.zip")
    deploy.set(true)
}

