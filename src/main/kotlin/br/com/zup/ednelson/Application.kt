package br.com.zup.ednelson

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zup.ednelson")
		.start()
}

