package top.soy_bottle.pso


/**
 * 一个抽象的空间
 */
abstract class Space<Holder : ParticleHolder<Res, P>, Res : Comparable<Res>, P : Particle<Res, P>> {
	val particles = arrayListOf<Holder>()
	
	/**
	 * 执行优化算法
	 */
	abstract fun optimize()
}

/**
 * 粒子的行为管理类
 */
interface ParticleHolder<Res : Comparable<Res>, P : Particle<Res, P>> {
	/**
	 * 当前粒子的步数
	 */
	val step: Int
	
	/**
	 * 当前粒子位置
	 */
	val current: P
	/**
	 * 当前粒子的值
	 */
	val currentRes: Res
	/**
	 * 获取粒子本身的最佳位置
	 * @return 粒子位置和值的配对
	 */
	fun getPBest(): Pair<Res, P>
	
	/**
	 * 获取粒子的历史记录
	 * @param step 第几步(0是初始位置)
	 * @return 粒子位置和值的配对
	 */
	fun stepAt(step: Int): Pair<Res, P>
	
	/**
	 * 对粒子应用一个新的向量
	 * 同时也会产生一个新的步骤
	 */
	fun applyVector(doubles: List<Double>)
}

/**
 * 一个粒子位置
 * @param P 粒子位置的递归泛型
 * @param Res 粒子计算后给出的可比较类型
 */
interface Particle<Res : Comparable<Res>, P : Particle<Res, P>> {
	/**
	 * 用于计算当前粒子和另一个粒子的距离
	 * @param another 另一个粒子
	 */
	fun distance(another: P): Double
	
	/**
	 * 粒子的维度空间
	 */
	val dimensions: Int
	
	/**
	 * 对于另一个粒子的位置和权重，产生一个新的向量
	 *
	 */
	fun genVector(another: P, weight: Double): List<Double>
	
	/**
	 * 获取当前粒子位置所携带的向量
	 */
	fun getVector(): List<Double>
	
	/**
	 * 计算粒子所在位置的值
	 */
	fun calcRes(): Res
}