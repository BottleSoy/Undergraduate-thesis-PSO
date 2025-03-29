package top.soy_bottle.pso

import org.ejml.data.DMatrixRMaj
import org.ejml.dense.row.CommonOps_DDRM
import org.ejml.dense.row.decomposition.eig.SwitchingEigenDecomposition_DDRM
import org.ejml.interfaces.decomposition.EigenDecomposition_F64


fun main() {
	val tables = arrayOf(
		"airline",
		"airplane",
		"airplane_type",
		"airport",
		"airport_geo",
		"airport_reachable",
		"booking",
		"employee",
		"flight",
		"flight_log",
		"flightschedule",
		"passenger",
		"passengerdetails"
	)
	
	operator fun DMatrixRMaj.set(s1: String, s2: String, value: Int) {
		val i1 = tables.indexOf(s1)
		val i2 = tables.indexOf(s2)
		this.set(i1, i2, value.toDouble())
	}
	
	val graphMatrix = DMatrixRMaj(tables.size, tables.size)
	graphMatrix.fill(-1.0) //没有关系设置为-1
	repeat(tables.size) {
		graphMatrix[it, it] = 0.0 //自关联为0
	}
	
	
	graphMatrix["airline", "airport"] = 1
	graphMatrix["airplane", "airplane_type"] = 1
	graphMatrix["airplane", "airline"] = 1

	graphMatrix["airport_geo", "airport"] = 1
	graphMatrix["airport_reachable", "airport"] = 1

	graphMatrix["booking", "flight"] = 1
	graphMatrix["booking", "passenger"] = 1
	
	graphMatrix["flight", "airport"] = 2
	graphMatrix["flight", "airline"] = 1
	graphMatrix["flight", "airplane"] = 1
	graphMatrix["flight", "flightschedule"] = 1

	graphMatrix["flight_log", "flight"] = 1
	graphMatrix["flight_log", "employee"] = 1
	graphMatrix["flightschedule", "airline"] = 1
	graphMatrix["flightschedule", "airport"] = 2

	graphMatrix["passengerdetails", "passenger"] = 1
	graphMatrix.print("%.0f")
	val det: Double = CommonOps_DDRM.det(graphMatrix)
	println("Determinant: ${String.format("%.2f", det)}")
	val eig: EigenDecomposition_F64<DMatrixRMaj> = SwitchingEigenDecomposition_DDRM(graphMatrix.numRows)
	eig.decompose(graphMatrix)

	println("Eigenvalues:")
	for (i in 0 until eig.numberOfEigenvalues) {
		println(
			String.format("%.2f", eig.getEigenvalue(i).getReal()) + " "
				+ String.format("%.2f", eig.getEigenvalue(i).getImaginary()) + "i"
		)
	}

	println("Eigenvectors:")
	for (i in 0 until eig.numberOfEigenvalues) {
		val v = eig.getEigenVector(i)
		v?.print("%.2f") ?: println(null)
	}

}


