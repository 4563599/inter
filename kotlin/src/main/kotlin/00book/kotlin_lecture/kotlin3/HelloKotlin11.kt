package com.shengsiyuan.kotlin3

import java.awt.event.WindowEvent
import java.awt.event.WindowListener

// 对象声明
object MyObject {
    fun method() = "hello world"
}

object MyObject2: WindowListener {
    /**
     * Invoked when a window is changed from a minimized
     * to a normal state.
     */
    override fun windowDeiconified(e: WindowEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Invoked when the user attempts to close the window
     * from the window's system menu.
     */
    override fun windowClosing(e: WindowEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Invoked when a window has been closed as the result
     * of calling dispose on the window.
     */
    override fun windowClosed(e: WindowEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Invoked when the Window is set to be the active Window. Only a Frame or
     * a Dialog can be the active Window. The native windowing system may
     * denote the active Window or its children with special decorations, such
     * as a highlighted title bar. The active Window is always either the
     * focused Window, or the first Frame or Dialog that is an owner of the
     * focused Window.
     */
    override fun windowActivated(e: WindowEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Invoked when a Window is no longer the active Window. Only a Frame or a
     * Dialog can be the active Window. The native windowing system may denote
     * the active Window or its children with special decorations, such as a
     * highlighted title bar. The active Window is always either the focused
     * Window, or the first Frame or Dialog that is an owner of the focused
     * Window.
     */
    override fun windowDeactivated(e: WindowEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Invoked the first time a window is made visible.
     */
    override fun windowOpened(e: WindowEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Invoked when a window is changed from a normal to a
     * minimized state. For many platforms, a minimized window
     * is displayed as the icon specified in the window's
     * iconImage property.
     * @see java.awt.Frame.setIconImage
     */
    override fun windowIconified(e: WindowEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

/*
    关于对象表达式与对象声明之间的区别：

    1. 对象表达式是立刻初始化或是执行的
    2. 对象声明是延迟初始化的，在首次访问的时候进行
    3. 伴生对象是在其所对应的类被加载时初始化的，对应于Java的静态初始化
 */

fun main(args: Array<String>) {
    println(MyObject.method())
}