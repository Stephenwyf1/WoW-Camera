package com.example.wowCamera.data

enum class OperationType(type:Int){

    skyProcess(0),styleProcess(1),denoiseProcess(2),enhanceProcess(3),noProcess(-1),configProcess(5),AItransfer(510);

    private var type:Int = type
    fun getType():Int{
        return type
    }

    fun changeAICode(code:Int){
        this.type = code
    }


}