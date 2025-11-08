package sc.hwd.sillot.kmp.pages

import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.views.Text

@Page("hi")
internal class HelloWorldPage : Pager() {
    
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                allCenter()
            }

            Text {
                attr {
                    text("Hello Kuikly")
                    fontSize(14f)
                }
            }
        }
    }
}