/*
 * This file is part of Dynamic Surroundings Unofficial, licensed under the MIT License (MIT).
 *
 * Copyright (c) Abastro
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.DynSurround.world;

import java.io.IOException;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.common.base.Throwables;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class WorldProviderWeatherHandle extends WorldProviderShimBase {
	
	private boolean patchWeather = false;

	public WorldProviderWeatherHandle(final World world, final WorldProvider provider) {
		super(world, provider);
		
		try {
			new ClassReader(provider.getClass().getName()).accept(new ClassVisitor(Opcodes.ASM5) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, 
						String signature, String[] exceptions) {
					if(name.equals("updateWeather")) {
						return new MethodVisitor(Opcodes.ASM5) {
							boolean isTheMethod = true;

							@Override
							public void visitParameter(String name, int access) {
								this.isTheMethod = false;
							}

					        @Override
					        public void visitMethodInsn(int opcode, String owner, 
					            String name, String desc) {
					        	if(!this.isTheMethod)
					        		return;

					        	try {
									if(desc.equals(Type.getMethodDescriptor(World.class.getMethod("updateWeatherBody")))) {
										patchWeather = true;
									}
								} catch (NoSuchMethodException exc) {
									Throwables.propagate(exc);
								}
					        }
						};
					}
					return null;
				}
			}, ClassReader.SKIP_DEBUG);
		} catch (IOException exc) {
			Throwables.propagate(exc);
		}
	}

	@Override
	public void resetRainAndThunder() {
		if(ModOptions.resetRainOnSleep)
			provider.resetRainAndThunder();
	}
	
	@Override
	public void updateWeather() {
		if(!worldObj.isRemote && this.patchWeather) {
			WorldHandler.updateWeatherBody(this.worldObj);
			return;
		}

		provider.updateWeather();
	}

}
