package blink

import chisel3._
import chisel3.util.log2Ceil
import chisel3.util.experimental.BoringUtils
import chisel3.probe
import chisel3.experimental.hierarchy._ // for @public @instantiable
import chisel3.experimental.hierarchy.core.{Definition, Instance}
import circt.stage.ChiselStage

//===== Synthesizable (bore, drive) ==================
class BlinknCount(limit: Int = 8) extends Module {
  
  val counterWidth = log2Ceil(limit)

  @public val io = IO(new Bundle {
      val en = Input(Bool())
      val isAdd = Input(Bool())
      val out = Output(Bool())
      val count = Output(UInt(counterWidth.W))
  })
  

  val counter = Module(new myCounter(limit))
    
  // Hierarchy direct connect
  io.out := counter.out
  
  // Child connect
  io.count := BoringUtils.bore(counter.counter)
  BoringUtils.drive(counter.en) := io.en
  
  // Non-child connect
  BoringUtils.drive(counter.add0.isAdd) := io.isAdd
  
  // deprecated example ==================
  // BoringUtils.addSink(io.count, "uniqueID")
  // end deprecated example ==============
  
}

//===== Simulate/Probe Only (tap, tapAndRead) ===============
class BlinknCountTap(limit: Int = 8) extends Module {

  val counterWidth = log2Ceil(limit)

  val io = IO(new Bundle {
      val en = Input(Bool())
      val isAdd = Input(Bool())
      val out = Output(Bool())
      val count = Output(UInt(counterWidth.W))
      val prob = probe.Probe(UInt(counterWidth.W))
  })
    
  val counter = Module(new myCounter(limit))
    
  BoringUtils.drive(counter.en) := io.en
        
  // Non-child connect
  BoringUtils.drive(counter.add0.isAdd) := io.isAdd
  
  // Define probe
  probe.define(io.prob, BoringUtils.tap(counter.counter))
  // probe.forceInitial(io.prob, 1.U)  => This will cause error bcause Probe is read-only
  
  io.out := counter.out
  // Define non-probe debug I/O
  io.count := BoringUtils.tapAndRead(counter.counter)
  
}

//===== Probe R/W (rwTap) ==================================
class BlinknCountRWTap(limit: Int = 8) extends Module {

  val counterWidth = log2Ceil(limit)

  val io = IO(new Bundle {
      val en = Input(Bool())
      val isAdd = Input(Bool())
      val out = Output(Bool())
      val count = Output(UInt(counterWidth.W))
      val prob = probe.RWProbe(UInt(counterWidth.W))
  })
    
  val counter = Module(new myCounter(limit))
    
  BoringUtils.drive(counter.en) := io.en
        
  // Un-hierarchy connect
  BoringUtils.drive(counter.add0.isAdd) := io.isAdd
  
  // Define R/W probe
  probe.define(io.prob, BoringUtils.rwTap(counter.counter))
  probe.forceInitial(io.prob, 1.U)
  
  io.out := counter.out
  // Define non-probe debug I/O
  io.count := BoringUtils.tapAndRead(counter.counter)
}

object BlinknCount extends App {
  ChiselStage.emitSystemVerilog(
    new BlinknCount(limit = 16), 
    firtoolOpts = Array("--strip-debug-info", "--disable-all-randomization", "--split-verilog", "-o", "generated")
  )
}

object BlinknCountTap extends App {
  ChiselStage.emitSystemVerilog(
    new BlinknCountTap(limit = 16), 
    firtoolOpts = Array("--strip-debug-info", "--disable-all-randomization", "--split-verilog", "-o", "generated")
  )
}

object BlinknCountRWTap extends App {
  ChiselStage.emitSystemVerilog(
    new BlinknCountRWTap(limit = 16), 
    firtoolOpts = Array("--strip-debug-info", "--disable-all-randomization", "--split-verilog", "-o", "generated")
  )
}