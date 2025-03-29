package top.soy_bottle.pso

import kotlin.random.Random

sealed interface Dimension<Source> {
	//维度名称
	val name: String
	
	//维度的方向数量
	val dims: Int
	
	//默认值
	val default: Source
	
	fun random(r: Random): Source
	
	//原始数据向维度数据的映射
	fun toValue(a: Source): List<Double>
	
	//维度内距离，注意是，带负数的，指的是a->b的方向
	fun distance(a: Source, b: Source): List<Double>
	
	//从维度数据向原始数据的映射
	fun backValue(d: List<Double>): Source
	
	//对原始数据在维度内的运动
	fun applyMove(source: Source, d: List<Double>): Source
	
	//是否合理的空间
	fun isInvalid(r: List<Double>): Boolean
}