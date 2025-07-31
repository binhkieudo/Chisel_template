package blink

import chisel3._
import chisel3.util.log2Ceil
import chisel3.util.experimental.BoringUtils
import chisel3.probe
import chisel3.experimental.hierarchy._ // for @public @instantiable
import chisel3.experimental.hierarchy.core.{Definition, Instance}
import circt.stage.ChiselStage
import collection.mutable.ListBuffer

//===== Synthesizable (bore, drive) ==================
class BlinknCount(limit: Int = 8) extends Module {
  
  val counterWidth = log2Ceil(limit)

  val io = IO(new Bundle {
      val en = Input(Bool())
      val isAdd = Input(Bool())
      val out = Output(Bool())
      val count = Output(UInt(counterWidth.W))
  })
  
  // 1. Create a list to hold module instances.
  val site = collection.mutable.ListBuffer[Module]()
  site += this
  
  // 2. Pass the list to the child's constructor.
  // myCounter and its children (myAdder) will add themselves to this list.
  val counter = Module(new myCounter(limit)(site, this))
    
  // Hierarchy direct connect
  io.out := counter.out
  
  // Child connect
  io.count := BoringUtils.bore(counter.counter)
  
  BoringUtils.drive(counter.en) := io.en 
  
  // Traverse the list to find all instances of `myAdder` and drive their `isAdd` signal.
  site.collect {
    case adder: myAdder => {
      if (adder.parent.isInstanceOf[blink.myCounter]) {
        BoringUtils.drive(adder.isAdd) := io.isAdd
      }
      else {
        BoringUtils.drive(adder.isAdd) := !io.isAdd
      }
    }
  }
  
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
    
  // 1. Create a list to hold module instances.
  val site = collection.mutable.ListBuffer[Module]()
  site += this
  
  // 2. Pass the list to the child's constructor.
  val counter = Module(new myCounter(limit)(site, this))
    
  BoringUtils.drive(counter.en) := io.en
        
  // 3. Now you can control modules by iterating through the populated list.
  // This is more robust than relying on a fixed hierarchy.
  // Here, we find all instances of `myAdder` in the list and drive their `isAdd` signal.
  site.collect {
    case adder: myAdder => BoringUtils.drive(adder.isAdd) := io.isAdd
  }
  
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
    
  // Create and pass the list here as well
  val site = collection.mutable.ListBuffer[Module]()
  site += this
  
  val counter = Module(new myCounter(limit)(site, this))
    
  BoringUtils.drive(counter.en) := io.en
        
  // Traverse the list to find all instances of `myAdder` and drive their `isAdd` signal.
  site.collect {
    case adder: myAdder => BoringUtils.drive(adder.isAdd) := io.isAdd
  }
  
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