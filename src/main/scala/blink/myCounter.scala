package blink

import chisel3._
import chisel3.util.log2Ceil
import chisel3.util.experimental.BoringUtils
import chisel3.experimental.hierarchy._ // for @public
import chisel3.experimental.hierarchy.core.{Definition, Instance}
import collection.mutable.ListBuffer

@instantiable
class myCounter(limit: Int = 8, moduleList: collection.mutable.ListBuffer[Module]) extends Module {
    
    moduleList += this

    val counterWidth = log2Ceil(limit)
    
    val out = IO(Output(Bool()))
    
    val en = RegInit(false.B)
    val counter = RegInit(0.U(counterWidth.W))
    
    // Un-hierarchy modules
    // Pass the list down to the child module so it can also register itself
    val add0 = Module(new myAdder(counterWidth, moduleList))
    
    add0.io.in0 := counter
    add0.io.in1 := 1.U
    
    val update_result = add0.io.out
    
    when (en) {
        counter := update_result
    }
    
    out := counter === (limit-1).U   
    
    // deprecated example ==================
    // BoringUtils.addSource(counter, "uniqueID")
    // end deprecated example ==============
}

class myAdder(width: Int = 4, moduleList: collection.mutable.ListBuffer[Module]) extends Module {

    moduleList += this

    val io = IO(new Bundle {
        val in0 = Input(UInt(width.W))
        val in1 = Input(UInt(width.W))
        val out = Output(UInt(width.W))
    })
    
    val isAdd = Wire(Bool())
    
    when (isAdd) {
        io.out := io.in0 + io.in1
    } .otherwise {
        io.out := io.in0 - io.in1
    }

}
