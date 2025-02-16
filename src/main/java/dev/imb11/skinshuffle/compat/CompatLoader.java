/*
 * ALL RIGHTS RESERVED
 *
 * Copyright (c) 2024 Calum H. (IMB11) and enjarai
 *
 * THE SOFTWARE IS PROVIDED "AS IS," WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package dev.imb11.skinshuffle.compat;

import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;

public class CompatLoader {
    private static final ArrayList<CompatHandler> HELPERS = new ArrayList<>();

    static {}

    public static void init() {
        for (CompatHandler helper : HELPERS) {
            if(FabricLoader.getInstance().isModLoaded(helper.getID()))
                helper.execute();
        }
    }
}
