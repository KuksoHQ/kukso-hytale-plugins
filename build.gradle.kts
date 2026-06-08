tasks.register("printModules") {
    group = "help"
    description = "Prints Kukso Hytale modules."
    doLast {
        subprojects.forEach { println("${it.path} -> ${it.projectDir}") }
    }
}
