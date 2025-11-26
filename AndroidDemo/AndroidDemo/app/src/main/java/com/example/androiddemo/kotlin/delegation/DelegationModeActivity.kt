package com.example.androiddemo.kotlin.delegation

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.androiddemo.R

/**
 * 更精简的示例：按钮触发委托打印，并把结果显示在界面上。
 */
class DelegationModeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delegation_mode)

        val logView: TextView = findViewById(R.id.tvResult)
        val runButton: Button = findViewById(R.id.btnPrint)

        runButton.setOnClickListener { logView.text = runDemoText() }
        logView.text = runDemoText()
    }

    private fun runDemoText(): String {
        val original = ConsolePrinter("委托打印 => 原始输出")
        val decorator = LoggingPrinter(original)
        return decorator.myPrint()
    }
}

interface MyPrinter {
    fun myPrint(): String
}

class ConsolePrinter(private val message: String) : MyPrinter {
    override fun myPrint(): String = message
}

class LoggingPrinter(private val inner: MyPrinter) : MyPrinter by inner {
    override fun myPrint(): String {
        return "=== 开始打印 ===\n${inner.myPrint()}\n=== 打印结束 ==="
    }
}
