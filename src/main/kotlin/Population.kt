import java.util.logging.Level
import kotlin.random.Random

class Population(startPop: String) : Loggable {
    val log = logger()
    val individuals: MutableList<Individual> =
        startPop.chunked(5) { gene -> Individual(gene.toString()) }.toMutableList()
    val fitnessSum = individuals.sumOf { it.fitness }

    init {
        if (Constants.logging)
            log.level = Level.ALL
        else
            log.level = Level.OFF

        log.info("Population: ${individuals.map { it.gene }}")
    }

    private fun roulette(): Individual {
        val random = Random.nextDouble(until = 1.0)
        val percentages = mutableMapOf<String, Double>()

        // make percentages based on the individual fitness and the overall fitness
        individuals.forEach {
            val percentage: Double = it.fitness.toDouble() / fitnessSum.toDouble()
            percentages[it.gene] = percentage
        }

        // determine fittest gene
        var sum = 0.0
        var fittest = ""
        for ((gene, percentage) in percentages) {
            sum += percentage
            if (random <= sum) {
                fittest = gene
                break
            }
        }

        return individuals.first { it.gene == fittest }
    }

    fun getWeakestAndFittest(): Pair<Individual, Individual> {
        log.info("Overall fitness: $fitnessSum")
        val rouletteResults = mutableListOf<Individual>()
        log.info("Running 4 iterations of roulette to choose the weakest and the fittest")
        for (i in 1..individuals.size) {
            rouletteResults.add(roulette())
        }
        // combine lists and choose gene which is least occurring
        val sum = individuals + rouletteResults
        val weakest = sum.minByOrNull { it.gene }!!
        val fittest = sum.maxByOrNull { it.gene }!!
        log.info("Weakest gene: ${weakest.gene} with fitness: ${weakest.fitness}")
        log.info("Fittest gene: ${fittest.gene} with fitness: ${fittest.fitness}")
        return Pair(weakest, fittest)
    }
}
