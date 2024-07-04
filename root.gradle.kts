plugins {
    id("fabric-loom") version "1.6-SNAPSHOT" apply false
    id("com.replaymod.preprocess") version "b09f534"
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://libraries.minecraft.net/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://github.com/jitsi/jitsi-maven-repository/raw/master/releases/")
        maven("https://maven.fabricmc.net/")
        maven("https://jitpack.io")
    }
}


preprocess {
    val mc12006 = createNode("1.20.6", 12006, "yarn")
    val mc12005 = createNode("1.20.5", 12005, "yarn")
    val mc12004 = createNode("1.20.4", 12004, "yarn")
    val mc12002 = createNode("1.20.2", 12002, "yarn")
    val mc12001 = createNode("1.20.1", 12001, "yarn")
    val mc11904 = createNode("1.19.4", 11904, "yarn")
    val mc11802 = createNode("1.18.2", 11802, "yarn")
    val mc11800 = createNode("1.18", 11800, "yarn")
    val mc11701 = createNode("1.17.1", 11701, "yarn")
    val mc11605 = createNode("1.16.5", 11605, "yarn")
    // val mc11700 = createNode("1.17", 11700, "yarn")
    // val mc11902 = createNode("1.19.2", 11902, "yarn")

    // IMPORTANT!!
    // When adding a version like (1.17 or 1.18.2)
    // Make sure to a corresponding directory in `./versions/`
    mc12006.link(mc12005)
    mc12005.link(mc12004)
    mc12004.link(mc12002)
    mc12002.link(mc12001)
    mc12001.link(mc11904)
    mc11904.link(mc11802)
    mc11802.link(mc11800)
    mc11800.link(mc11701)
    mc11701.link(mc11605)

    //mc11902.link(mc11802)
    // mc11700.link(mc11605)

}
