/* According to the ARM EABI, all registers have undefined values at
 * program startup except:
 *
 * - the instruction pointer (r15)
 * - the stack pointer (r13)
 * - the rtld_fini pointer (r0)
 */

#include <cstdio>
#include "arch.h"

#define BRANCH(stack_pointer, destination) do {			\
	asm volatile (						\
		"// Restore initial stack pointer.	\n\t"	\
		"mov sp, %0				\n\t"	\
		"					\n\t"	\
		"// Clear rtld_fini.			\n\t"	\
		"mov r0, #0				\n\t"	\
		"					\n\t"	\
		"// Start the program.			\n\t"	\
		"mov pc, %1				\n"	\
		: /* no output */				\
		: "r" (stack_pointer), "r" (destination)	\
		: "memory", "sp", "r0", "pc");			\
	__builtin_unreachable();				\
	} while (0)

#define PREPARE_ARGS_1(arg1_)				\
	register word_t arg1 asm("r0") = (word_t)arg1_;

#define PREPARE_ARGS_2(arg1_, arg2_)		\
	PREPARE_ARGS_1(arg1_)				\
	register word_t arg2 asm("r1") = (word_t)arg2_;

#define PREPARE_ARGS_3(arg1_, arg2_, arg3_)		\
	PREPARE_ARGS_2(arg1_, arg2_)				\
	register word_t arg3 asm("r2") = (word_t)arg3_;

#define PREPARE_ARGS_4(arg1_, arg2_, arg3_, arg4_)	\
	PREPARE_ARGS_3(arg1_, arg2_, arg3_)				\
	register word_t arg4 asm("r3") = (word_t)arg4_;

#define PREPARE_ARGS_5(arg1_, arg2_, arg3_, arg4_, arg5_)	\
	PREPARE_ARGS_4(arg1_, arg2_, arg3_, arg4_)				\
	register word_t arg5 asm("r4") = (word_t)arg5_;

#define PREPARE_ARGS_6(arg1_, arg2_, arg3_, arg4_, arg5_, arg6_)	\
	PREPARE_ARGS_5(arg1_, arg2_, arg3_, arg4_, arg5_)				\
	register word_t arg6 asm("r5") = (word_t)arg6_;

#define OUTPUT_CONTRAINTS_1			\
	"r" (arg1)

#define OUTPUT_CONTRAINTS_2			\
    OUTPUT_CONTRAINTS_1,			\
	"r" (arg2)

#define OUTPUT_CONTRAINTS_3			\
	OUTPUT_CONTRAINTS_2,			\
	"r" (arg3)

#define OUTPUT_CONTRAINTS_4			\
	OUTPUT_CONTRAINTS_3,			\
	"r" (arg4)

#define OUTPUT_CONTRAINTS_5			\
	OUTPUT_CONTRAINTS_4,			\
	"r" (arg5)

#define OUTPUT_CONTRAINTS_6			\
	OUTPUT_CONTRAINTS_5,			\
	"r" (arg6)

#define SVC_SYSCALL(number_, nb_args, args...)			\
	({							\
		register word_t number asm("r7") = number_;	\
		register word_t result asm("r0");		\
		PREPARE_ARGS_##nb_args(args)			\
			asm volatile (				\
				"svc #0x00000000	\n\t"	\
				: "=r" (result)			\
				: "r" (number),			\
				OUTPUT_CONTRAINTS_##nb_args	\
				: "memory");			\
			result;					\
	})

struct linux_dirent {
    unsigned long d_ino;/* Inode number */
    unsigned long d_off;/* Offset to next linux dirent */
    unsigned short d_reclen;/* Length of this linux dirent */
    char d_name[];/* Filename (null-terminated) */
};

#define SVC_OPENAT      322      // 0x142
#define SVC_CLOSE	    6        // 0x6
#define SVC_GETDENTS    217      // 0xd9
#define SVC_READ        3        // 0x3
#define SVC_WRITE       4        // 0x4
#define SVC_READLINKAT  332      // 0x14c
#define SVC_FSTATAT	    327      // 0x147
