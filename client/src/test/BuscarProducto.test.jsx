import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import BuscarProducto from '../pages/BuscarProducto'

vi.mock('../api/client', () => ({
  default: {
    get: vi.fn(),
  },
}))

import client from '../api/client'

beforeEach(() => vi.clearAllMocks())

const stratocaster = {
  id: 1,
  name: 'Fender Stratocaster American Pro II',
  description: 'Guitarra eléctrica con pastillas V-Mod II',
  price: 1500,
  category: 'Guitarras',
  imageUrl: 'strat.jpg',
  stock: 8,
}

function renderBuscar(search = '') {
  return render(
    <MemoryRouter initialEntries={[`/buscar${search}`]}>
      <BuscarProducto />
    </MemoryRouter>
  )
}

// -------------------------------------------------------------------------
// Búsqueda por nombre
// -------------------------------------------------------------------------

test('muestra los datos del producto al buscarlo por nombre', async () => {
  client.get.mockResolvedValueOnce({ data: stratocaster })

  renderBuscar()

  await userEvent.type(screen.getByRole('textbox'), 'Fender Stratocaster American Pro II')
  await userEvent.click(screen.getByRole('button', { name: /buscar/i }))

  await waitFor(() => {
    expect(screen.getByText('Fender Stratocaster American Pro II')).toBeInTheDocument()
    expect(screen.getByText('$1500')).toBeInTheDocument()
    expect(screen.getByText(/Guitarras/)).toBeInTheDocument()
  })
})

test('muestra error si el producto no existe', async () => {
  client.get.mockRejectedValueOnce(new Error('Not found'))

  renderBuscar()

  await userEvent.type(screen.getByRole('textbox'), 'Gibson Les Paul Custom')
  await userEvent.click(screen.getByRole('button', { name: /buscar/i }))

  await waitFor(() => {
    expect(screen.getByText(/producto no encontrado/i)).toBeInTheDocument()
  })
})

test('muestra error si el campo de búsqueda está vacío', async () => {
  renderBuscar()

  await userEvent.click(screen.getByRole('button', { name: /buscar/i }))

  expect(screen.getByText(/ingresá un valor/i)).toBeInTheDocument()
  expect(client.get).not.toHaveBeenCalled()
})

// -------------------------------------------------------------------------
// Búsqueda por ID
// -------------------------------------------------------------------------

test('busca por ID al seleccionar el modo ID', async () => {
  client.get.mockResolvedValueOnce({ data: stratocaster })

  renderBuscar('?modo=id')

  await userEvent.type(screen.getByRole('textbox'), '1')
  await userEvent.click(screen.getByRole('button', { name: /buscar/i }))

  await waitFor(() => {
    expect(client.get).toHaveBeenCalledWith('/products/1')
  })
})

test('muestra badge de stock bajo si el stock es 3 o menos', async () => {
  client.get.mockResolvedValueOnce({ data: { ...stratocaster, stock: 2 } })

  renderBuscar()

  await userEvent.type(screen.getByRole('textbox'), 'Fender Stratocaster American Pro II')
  await userEvent.click(screen.getByRole('button', { name: /buscar/i }))

  await waitFor(() => {
    expect(screen.getByText(/stock bajo/i)).toBeInTheDocument()
  })
})

// -------------------------------------------------------------------------
// Cambio de modo
// -------------------------------------------------------------------------

test('al cambiar de modo se limpia el resultado anterior', async () => {
  client.get.mockResolvedValueOnce({ data: stratocaster })

  renderBuscar()

  await userEvent.type(screen.getByRole('textbox'), 'Fender Stratocaster American Pro II')
  await userEvent.click(screen.getByRole('button', { name: /buscar/i }))
  await waitFor(() => expect(screen.getByText('Fender Stratocaster American Pro II')).toBeInTheDocument())

  await userEvent.click(screen.getByRole('button', { name: /por id/i }))

  expect(screen.queryByText('Fender Stratocaster American Pro II')).not.toBeInTheDocument()
})
