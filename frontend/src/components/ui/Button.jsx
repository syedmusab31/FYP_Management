import React from 'react';
import { cn } from '../../utils/cn';
import { Loader2 } from 'lucide-react';

const Button = React.forwardRef(({ className, variant = 'primary', size = 'default', children, isLoading, disabled, ...props }, ref) => {
    const variants = {
        primary: 'bg-blue-600 text-white hover:bg-blue-700 shadow-sm hover:shadow-md active:scale-95',
        secondary: 'bg-indigo-600 text-white hover:bg-indigo-700 shadow-sm hover:shadow-md active:scale-95',
        outline: 'border-2 border-slate-200 bg-transparent hover:bg-slate-50 text-slate-900',
        ghost: 'bg-transparent hover:bg-slate-100 text-slate-700 hover:text-slate-900',
        danger: 'bg-red-500 text-white hover:bg-red-600 shadow-sm hover:shadow-md',
    };

    const sizes = {
        sm: 'h-8 px-3 text-xs',
        default: 'h-10 px-4 py-2',
        lg: 'h-12 px-8 text-lg',
        icon: 'h-10 w-10 p-2 flex items-center justify-center',
    };

    return (
        <button
            ref={ref}
            disabled={disabled || isLoading}
            className={cn(
                'inline-flex items-center justify-center font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none rounded-none',
                variants[variant],
                sizes[size],
                className
            )}
            {...props}
        >
            {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
            {children}
        </button>
    );
});

Button.displayName = "Button";
export default Button;
