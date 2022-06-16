class Individual(var gene: String) {

    private val x = Integer.parseInt(gene, 2)
    val fitness: Int = x * x
}