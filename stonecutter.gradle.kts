plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.11"  // Default active version

stonecutter registerChiseled tasks.register("buildAllVersions", stonecutter.chiseled) {
    group = "build"
    ofTask("build")
}
