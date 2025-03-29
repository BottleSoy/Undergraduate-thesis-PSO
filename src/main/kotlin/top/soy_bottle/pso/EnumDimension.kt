package top.soy_bottle.pso



import top.soy_bottle.pso.Dimension
import kotlin.random.Random


fun List<Double>.maxIndexAndValue(): Pair<Int, Double> {
	var max = .0
	var maxIndex = 0
	forEachIndexed { i, v ->
		if (v > max) {
			max = v
			maxIndex = i
		}
	}
	return maxIndex to max
}

class EnumDimension<E : Enum<E>>(override val name: String, clazz: Class<E>, override val default: E) : Dimension<E> {
	val values: Array<E> = clazz.enumConstants
	override val dims: Int = values.size
	override fun random(r: Random): E =values.random(r)
	
	
	override fun toValue(a: E): List<Double> {
		return buildList {
			repeat(values.size) {
				if (values[it] == a) {
					this.add(1.0)
				} else {
					this.add(0.0)
				}
			}
		}
	}
	
	override fun backValue(d: List<Double>): E {
		return values[d.maxIndexAndValue().first]
	}
	
	override fun isInvalid(r: List<Double>) = r.any { it > 1.5 || it < 0.0 }
	
	override fun applyMove(source: E, d: List<Double>): E {
		val myDim = d[values.indexOf(source)]
		val (index, value) = d.maxIndexAndValue()
		return if (value > 0.5 && value - myDim > 0.5) values[index] else source
	}
	
	override fun distance(a: E, b: E): List<Double> {
		return if (a != b) {
			buildList {
				repeat(values.size) {
					this.add(.0)
				}
			}
		} else {
			buildList {
				repeat(values.size) {
					if (values[it] == a || values[it] == b) this.add(1.0)
					else this.add(.0)
				}
			}
		}
	}
	
}