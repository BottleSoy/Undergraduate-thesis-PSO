package top.soy_bottle.pso

import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.random.nextInt

//连续空间(浮点)
class FloatDimension(
	override val name: String,
	val range: ClosedFloatingPointRange<Double>,
	override val default: Double,
) : Dimension<Double> {
	override val dims: Int = 1
	override fun random(r: Random) = r.nextDouble(range.start, range.endInclusive)
	
	override fun toValue(a: Double): List<Double> = listOf(a)
	
	override fun backValue(d: List<Double>) = d[0]
	
	override fun isInvalid(r: List<Double>) = r[0] in range
	
	override fun applyMove(source: Double, d: List<Double>) = (source + d[0]).withIn(range.endInclusive, range.start)
	
	override fun distance(a: Double, b: Double) = listOf(b - a)
	
}

//连续空间(整形)
open class IntDimension(
	override val name: String,
	val range: IntRange = Int.MIN_VALUE..Int.MAX_VALUE,
	override val default: Int,
) : Dimension<Int> {
	override val dims: Int = 1
	override fun random(r: Random) = r.nextInt(range)
	
	override fun toValue(a: Int): List<Double> = listOf(a.toDouble())
	
	override fun backValue(d: List<Double>) = d[0].roundToInt()
	
	override fun isInvalid(r: List<Double>) = r[0].roundToInt() in range
	
	override fun applyMove(source: Int, d: List<Double>) =
		(source + d[0]).roundToInt().withIn(range.last, range.first)
	
	override fun distance(a: Int, b: Int) = listOf((b - a).toDouble())
	
}

fun Int.withIn(max: Int, min: Int) = if (min > this) min else if (max < this) max else this
fun Double.withIn(max: Double, min: Double) = if (min > this) min else if (max < this) max else this

class BooleanDimension(override val name: String, override val default: Boolean) : Dimension<Boolean> {
	override val dims: Int = 1
	override fun random(r: Random): Boolean = r.nextBoolean()
	
	override fun toValue(a: Boolean): List<Double> = listOf(if (a) 1.0 else 0.0)
	
	override fun backValue(d: List<Double>) = d[0] <= 0.5
	
	override fun isInvalid(r: List<Double>) = 0 >= r[0] || 1 <= r[0]
	
	override fun applyMove(source: Boolean, d: List<Double>) = if (source) d[0] <= -0.5 else d[0] >= 0.5
	
	override fun distance(a: Boolean, b: Boolean) = listOf(if (a == b) .0 else if (a) 1.0 else -1.0)
}