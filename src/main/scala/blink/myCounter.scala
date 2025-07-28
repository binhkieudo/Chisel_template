package blink

import chisel3._
import chisel3.util._

class myCounter(limit: Int = 8) extends Module {
    
    val counterWidth = log2Ceil(limit)
    
    val io = IO(new Bundle {
        val en = Input(Bool())
        val out = Output(UInt(counterWidth.W))
    })
    
    val counter = RegInit(0.U(counterWidth.W))
    
    when (io.en) {
        counter := counter + 1.U
    }
    
    io.out := counter
    
}
