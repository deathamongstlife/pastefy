import type { Preset } from '@primeuix/themes/types'

export default {
  semantic: {
    primary: {
      50: '#f0e6ff',
      100: '#d4b3ff',
      200: '#b580ff',
      300: '#964dff',
      400: '#8B00FF', // cyber-purple
      500: '#7a00e6',
      600: '#6900cc',
      700: '#5800b3',
      800: '#470099',
      900: '#360080',
      950: '#250066',
    },
    colorScheme: {
      light: {
        primary: {
          color: '#8B00FF',
          inverseColor: '#ffffff',
          hoverColor: '#7a00e6',
          activeColor: '#6900cc',
        },
        surface: {
          0: '#ffffff',
          50: '#f8f9fa',
          100: '#f1f3f5',
          200: '#e9ecef',
          300: '#dee2e6',
          400: '#ced4da',
          500: '#adb5bd',
          600: '#868e96',
          700: '#495057',
          800: '#343a40',
          900: '#212529',
          950: '#151932',
        },
      },
      dark: {
        primary: {
          color: '#00F5FF', // cyber-blue
          inverseColor: '#0A0E27',
          hoverColor: '#00d4e6',
          activeColor: '#00b3cc',
        },
        surface: {
          0: '#0A0E27', // dark-bg
          50: '#151932', // card-bg
          100: '#1E2340', // hover-bg
          200: '#272d4d',
          300: '#30375a',
          400: '#394167',
          500: '#424b74',
          600: '#5a6285',
          700: '#727996',
          800: '#8a90a7',
          900: '#a2a7b8',
          950: '#babec9',
        },
      },
    },
  },
  primitive: {
    colorScheme: {
      light: {
        surface: {
          0: '#ffffff',
          50: '{zinc.50}',
          100: '{zinc.100}',
          200: '{zinc.200}',
          300: '{zinc.300}',
          400: '{zinc.400}',
          500: '{zinc.500}',
          600: '{zinc.600}',
          700: '{zinc.700}',
          800: '{zinc.800}',
          900: '{zinc.900}',
          950: '{zinc.950}',
        },
      },
      dark: {
        surface: {
          0: '#0A0E27',
          50: '#151932',
          100: '#1E2340',
          200: '#272d4d',
          300: '#30375a',
          400: '#394167',
          500: '#424b74',
          600: '#5a6285',
          700: '#727996',
          800: '#8a90a7',
          900: '#a2a7b8',
          950: '#babec9',
        },
      },
    },
  },
  components: {
    inputtext: {
      colorScheme: {
        light: {
          shadow: 'none',
          background: 'transparent',
          border: {
            color: '{neutral.400}',
          },
        },
        dark: {
          background: 'rgba(21, 25, 50, 0.8)',
          border: {
            color: '#00F5FF',
          },
        },
      },
    },
    select: {
      colorScheme: {
        light: {
          shadow: 'none',
          background: 'transparent',
          border: {
            color: '{neutral.400}',
          },
        },
        dark: {
          background: 'rgba(21, 25, 50, 0.8)',
          border: {
            color: '#00F5FF',
          },
        },
      },
    },
    tabs: {
      tab: {
        padding: '12px 12px',
      },
    },
    treeselect: {
      colorScheme: {
        light: {
          shadow: 'none',
          background: 'transparent',
          border: {
            color: '{neutral.400}',
          },
        },
        dark: {
          background: 'rgba(21, 25, 50, 0.8)',
          border: {
            color: '#00F5FF',
          },
        },
      },
    },
  },
} as Preset
