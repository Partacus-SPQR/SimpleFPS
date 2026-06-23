plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.2" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("buildAllVersions", stonecutter.chiseled) {
    group = "build"
    ofTask("build")
}
