import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import java.util.logging.Level

fun main(args: Array<String>) {
    val parser = ArgParser("GeneticAlgorithm")
    val population by parser.option(ArgType.String, shortName = "p", description = "Start population").required()
    val verbose by parser.option(ArgType.Boolean, shortName = "v", description = "Verbose mode").default(false)
    val iterations by parser.option(ArgType.Int, shortName = "i", description = "Iterations to run").default(3)

    parser.parse(args)

    Constants.logging = verbose

    var startPopulation = Population(population)
    println("Start population: ${startPopulation.individuals.map { it.gene }} Overall fitness: ${startPopulation.fitnessSum}")
    val demo = Main()
    for (i in 1..iterations) {
        demo.selection(startPopulation)
        startPopulation = demo.crossover(startPopulation)
        if (i == iterations)
            println("Final population: ${startPopulation.individuals.map { it.gene }}, Overall fitness: ${startPopulation.fitnessSum}")
    }
}

class Main : Loggable {
    private val log = logger()

    init {
        if (Constants.logging)
            log.level = Level.ALL
        else
            log.level = Level.OFF
    }

    fun selection(population: Population) {
        val (weakest, fittest) = population.getWeakestAndFittest()
        replaceLeastFittest(population, weakest, fittest)
    }

    fun crossover(population: Population): Population {
        // 1 point crossover
        val crossover1 = (1..4).random()
        var crossover2 = (1..4).random()
        while (crossover2 == crossover1) // make sure there are two different crossover points
            crossover2 = (1..4).random()

        // mutate
        population.individuals.forEach { mutate(it) }

        // crossover genes
        log.info("Crossing over genes at random points. Crossover point 1: $crossover1, Crossover point 2: $crossover2")

        val crossoverChunks = mutableListOf<String>()

        for ((i, individual) in population.individuals.withIndex()) {
            if (i % 2 == 0)
                individual.gene.splitAt(crossover1).forEach { crossoverChunks.add(it) }
            else
                individual.gene.splitAt(crossover2).forEach { crossoverChunks.add(it) }
        }

        crossoverChunks.sortBy { it.length }
        log.info("Creating new population from chunks: $crossoverChunks")

        val crossovers = mutableListOf<String>()

        // concat list, first element with last, second element with second last, etc.
        for (i in 0 until population.individuals.size) {
            crossovers.add(crossoverChunks[i] + crossoverChunks[(crossoverChunks.size - 1) - i])
        }

        val newPopString = crossovers.joinToString("")

        // if population not each distinct, crossover again, else return
        return if (newPopString.chunked(5).distinct().size < 4)
            crossover(population) // crossover with the same population
        else
            Population(newPopString) // return new population
    }

    private fun mutate(individual: Individual) {
        val mutationRate = (1..100).random() // random number, 1% mutation rate
        val random = (1..100).random()
        if (random == mutationRate) {
            val index = (0 until individual.gene.length).random() // bit to flip
            val chars = individual.gene.toCharArray()
            //flip bit
            if (chars[index] == '1')
                chars[index] = '0'
            else
                chars[index] = '1'

            log.info("Mutation: flipped bit on index $index.")
            log.info("Before mutation: ${individual.gene}, after mutation: ${chars.joinToString("")}")
            individual.gene = chars.joinToString("")
        }
    }

    private fun replaceLeastFittest(population: Population, oldIndividual: Individual, newIndividual: Individual) {
        val index = population.individuals.indexOf(oldIndividual)
        population.individuals[index] = newIndividual
        log.info("Replaced weakest gene: ${oldIndividual.gene} with fittest: ${newIndividual.gene}")
    }

    // extension function to get both parts of the string after split
    private fun String.splitAt(index: Int): List<String> =
        listOf(this.substring(0 until index), this.substring(index until this.length))
}
