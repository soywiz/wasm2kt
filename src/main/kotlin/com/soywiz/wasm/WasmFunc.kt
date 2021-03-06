package com.soywiz.wasm

interface WasmFuncResolver {
    fun resolveFunc(name: String): WasmFunc
}

data class FuncWithType(val name: String, val func: WasmType.Function)

interface WasmFuncRef {
    val name: String
    val func: WasmFunc
}

data class WasmFuncName(override val name: String, val resolve: (name: String) -> WasmFunc) : WasmFuncRef {
    override val func get() = resolve(name)
}

data class WasmFuncWithType(val name: String, val type: WasmType.Function)

data class WasmFunc(
    val index: Int,
    val type: WasmType.Function,
    var code: WasmCode? = null,
    var import: WasmImport? = null,
    var export: WasmExport? = null,
    var code2: WasmCode2? = null,
    val name2: String? = null
) : WasmFuncRef {
    fun getAst(wasm: WasmModule): Wast.Stm? = when {
        code != null -> {
            val body = code!!.body
            val bodyAst = body.toAst(wasm, func)
            bodyAst
        }
        code2 != null -> code2!!.body
        else -> null
    }

    override val func = this
    val rlocals: List<AstLocal> by lazy { type.args + (code?.flatLocals ?: listOf()) }

    override val name: String by lazy { name2 ?: import?.name ?: export?.name ?: "f$index" }

    val ftype: FuncWithType by lazy { FuncWithType(name, type) }
    val fwt = WasmFuncWithType(name, type)
}
