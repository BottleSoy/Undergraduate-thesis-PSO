package top.soy_bottle.pso

interface TypeName {
	val type: String
}

enum class RelipaRead(override val type: String):TypeName {
	LEADER("'leader'"),
	FOLLOWER("'follower'"),
	PREFER_LEADER("'prefer-leader'"),
	CLOEST_REPLICAS("'closest-replicas'"),
	CLOEST_ADAPTIVE("'closest-adaptive'"),
	
}