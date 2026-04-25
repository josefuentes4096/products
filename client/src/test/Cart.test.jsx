import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import Cart from '../components/Cart'

const mockDisminuir   = vi.fn()
const mockAgregar     = vi.fn()
const mockEliminar    = vi.fn()
const mockVaciar      = vi.fn()
const mockConfirmar   = vi.fn()

let mockProductos     = []
let mockPedidoExitoso = false
let mockPedidoError   = null

vi.mock('../contexts/CarritoContext', () => ({
  useCarrito: () => ({
    productos:       mockProductos,
    agregarProducto: mockAgregar,
    disminuirProducto: mockDisminuir,
    eliminarProducto:  mockEliminar,
    vaciarCarrito:     mockVaciar,
    confirmarPedido:   mockConfirmar,
    pedidoExitoso:     mockPedidoExitoso,
    pedidoError:       mockPedidoError,
  }),
}))

const guitarra = { id: 1, name: 'Fender Stratocaster', price: 1500, cantidad: 2, imageUrl: '' }
const pedal    = { id: 2, name: 'Boss DS-1',           price: 80,   cantidad: 1, imageUrl: '' }

beforeEach(() => {
  vi.clearAllMocks()
  mockProductos     = []
  mockPedidoExitoso = false
  mockPedidoError   = null
})

// -------------------------------------------------------------------------
// Carrito vacío
// -------------------------------------------------------------------------

test('muestra mensaje cuando el carrito está vacío', () => {
  mockProductos = []

  render(<Cart />)

  expect(screen.getByText(/el carrito está vacío/i)).toBeInTheDocument()
})

// -------------------------------------------------------------------------
// Carrito con productos
// -------------------------------------------------------------------------

test('muestra los productos con nombre, precio unitario y cantidad', () => {
  mockProductos = [guitarra]

  render(<Cart />)

  expect(screen.getByText('Fender Stratocaster')).toBeInTheDocument()
  expect(screen.getByText(/precio unitario: \$1500/i)).toBeInTheDocument()
  expect(screen.getByText('2')).toBeInTheDocument()
})

test('muestra el subtotal de cada producto', () => {
  mockProductos = [guitarra]

  render(<Cart />)

  // 1500 * 2 = 3000.00
  expect(screen.getByText(/subtotal: \$3000\.00/i)).toBeInTheDocument()
})

test('muestra el total del carrito', () => {
  mockProductos = [guitarra, pedal]

  render(<Cart />)

  // 1500*2 + 80*1 = 3080.00
  expect(screen.getByText(/total: \$3080\.00/i)).toBeInTheDocument()
})

// -------------------------------------------------------------------------
// Acciones sobre items
// -------------------------------------------------------------------------

test('llama a disminuirProducto al hacer clic en -', async () => {
  mockProductos = [guitarra]

  render(<Cart />)

  await userEvent.click(screen.getByRole('button', { name: '-' }))

  expect(mockDisminuir).toHaveBeenCalledWith(1)
})

test('llama a agregarProducto al hacer clic en +', async () => {
  mockProductos = [guitarra]

  render(<Cart />)

  await userEvent.click(screen.getByRole('button', { name: '+' }))

  expect(mockAgregar).toHaveBeenCalledWith(guitarra)
})

test('llama a eliminarProducto al hacer clic en Eliminar', async () => {
  mockProductos = [guitarra]

  render(<Cart />)

  await userEvent.click(screen.getByRole('button', { name: /eliminar/i }))

  expect(mockEliminar).toHaveBeenCalledWith(1)
})

// -------------------------------------------------------------------------
// Confirmar y vaciar
// -------------------------------------------------------------------------

test('llama a confirmarPedido al hacer clic en Confirmar Pedido', async () => {
  mockProductos = [guitarra]

  render(<Cart />)

  await userEvent.click(screen.getByRole('button', { name: /confirmar pedido/i }))

  expect(mockConfirmar).toHaveBeenCalled()
})

test('llama a vaciarCarrito al hacer clic en Vaciar Carrito', async () => {
  mockProductos = [guitarra]

  render(<Cart />)

  await userEvent.click(screen.getByRole('button', { name: /vaciar carrito/i }))

  expect(mockVaciar).toHaveBeenCalled()
})

// -------------------------------------------------------------------------
// Alertas de estado
// -------------------------------------------------------------------------

test('muestra alerta de éxito cuando pedidoExitoso es true', () => {
  mockProductos     = []
  mockPedidoExitoso = true

  render(<Cart />)

  expect(screen.getByText(/pedido confirmado con éxito/i)).toBeInTheDocument()
})

test('muestra alerta de error cuando hay pedidoError', () => {
  mockProductos   = [guitarra]
  mockPedidoError = 'No se pudo confirmar el pedido. Intente nuevamente.'

  render(<Cart />)

  expect(screen.getByText(/no se pudo confirmar el pedido/i)).toBeInTheDocument()
})
