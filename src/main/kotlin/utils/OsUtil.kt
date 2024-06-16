package utils

import java.util.*


object OsUtil
{
    // Operating systems.
    private var os: OS? = null

    fun getOs(): OS
    {
        if (os == null)
        {
            val operSys = System.getProperty("os.name").lowercase(Locale.getDefault())
            if (operSys.contains("win"))
            {
                os = OS.WINDOWS
            }
            else if (operSys.contains("nix") || operSys.contains("nux")
                || operSys.contains("aix"))
            {
                os = OS.LINUX
            }
            else if (operSys.contains("mac"))
            {
                os = OS.MAC
            }
            else if (operSys.contains("sunos"))
            {
                os = OS.SOLARIS
            }
        }
        return os!!
    }


    enum class OS
    {
        WINDOWS, LINUX, MAC, SOLARIS
    }
}