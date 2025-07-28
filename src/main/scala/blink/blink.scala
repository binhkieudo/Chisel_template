package blink

import chisel3._
import chisel3.util.{circt => _, _}

import circt.stage.ChiselStage

class Blink(limit: Int = 8) extends Module {

    val io = IO(new Bundle {
        val en = Input(Bool())
        val out = Output(Bool())
    })
    
    val counter = Module(new myCounter(limit))
    
    val cmp = RegNext(counter.io.out === (limit-1).U)
    
    counter.io.en := io.en
    
    io.out := cmp
    
}

object Blink extends App {
  ChiselStage.emitSystemVerilog(
    new Blink(limit = 16), 
    firtoolOpts = Array("--strip-debug-info", "--disable-all-randomization", "--split-verilog", "-o", "generated")
  )
}
