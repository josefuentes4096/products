import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import FormularioProducto from '../components/FormularioProducto'

vi.mock('../api/client', () => ({
  default: { post: vi.fn() },
}))

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal()
  return { ...actual, useNavigate: () => mockNavigate }
})

import client from '../api/client'

function renderFormulario() {
  return render(
    <MemoryRouter>
      <FormularioProducto />
    </MemoryRouter>
  )
}

async function completarFormulario({ name = '', price = '', description = '', category = '', stock = '', imageUrl = '' } = {}) {
  if (name)        await userEvent.type(screen.getByLabelText('Nombre'), name)
  if (price)       await userEvent.type(screen.getByLabelText('Precio'), price)
  if (description) await userEvent.type(screen.getByLabelText('Descripción'), description)
  if (category)    await userEvent.type(screen.getByLabelText('Categoría'), category)
  if (stock)       await userEvent.type(screen.getByLabelText('Stock'), stock)
  if (imageUrl)    await userEvent.type(screen.getByLabelText('URL de imagen (opcional)'), imageUrl)
}

// -------------------------------------------------------------------------
// Validaciones
// -------------------------------------------------------------------------

test('muestra error si el nombre está vacío', async () => {
  renderFormulario()

  await userEvent.click(screen.getByRole('button', { name: /guardar/i }))

  expect(screen.getByText(/nombre es obligatorio/i)).toBeInTheDocument()
  expect(client.post).not.toHaveBeenCalled()
})

test('muestra error si el precio es 0', async () => {
  renderFormulario()

  await completarFormulario({ name: 'Vox AC30', price: '0' })
  await userEvent.click(screen.getByRole('button', { name: /guardar/i }))

  expect(screen.getByText(/precio debe ser mayor/i)).toBeInTheDocument()
})

test('muestra error si el precio es negativo', async () => {
  renderFormulario()

  await completarFormulario({ name: 'Marshall DSL40', price: '-100' })
  await userEvent.click(screen.getByRole('button', { name: /guardar/i }))

  expect(screen.getByText(/precio debe ser mayor/i)).toBeInTheDocument()
})

test('muestra error si la descripción tiene menos de 10 caracteres', async () => {
  renderFormulario()

  await completarFormulario({ name: 'Marshall DSL40', price: '1200', description: 'Corta' })
  await userEvent.click(screen.getByRole('button', { name: /guardar/i }))

  expect(screen.getByText(/al menos 10 caracteres/i)).toBeInTheDocument()
})

test('muestra error si el stock es negativo', async () => {
  renderFormulario()

  await completarFormulario({ name: 'Marshall DSL40', price: '1200', description: 'Amplificador valvular 40W', stock: '-1' })
  await userEvent.click(screen.getByRole('button', { name: /guardar/i }))

  expect(screen.getByText(/stock no puede ser negativo/i)).toBeInTheDocument()
})

// -------------------------------------------------------------------------
// Envío exitoso
// -------------------------------------------------------------------------

test('llama a POST /products con los datos correctos al guardar', async () => {
  client.post.mockResolvedValueOnce({ data: { id: 4 } })

  renderFormulario()

  await completarFormulario({
    name: 'Vox AC30',
    price: '1800',
    description: 'Amplificador valvular de 30W icono del sonido britanico',
    category: 'Amplificadores',
    stock: '2',
    imageUrl: 'vox_ac30.jpg',
  })
  await userEvent.click(screen.getByRole('button', { name: /guardar/i }))

  await waitFor(() => {
    expect(client.post).toHaveBeenCalledWith('/products', {
      name: 'Vox AC30',
      price: 1800,
      description: 'Amplificador valvular de 30W icono del sonido britanico',
      category: 'Amplificadores',
      stock: 2,
      imageUrl: 'vox_ac30.jpg',
    })
  })
})

test('redirige a /admin tras guardar exitosamente', async () => {
  client.post.mockResolvedValueOnce({ data: { id: 4 } })

  renderFormulario()

  await completarFormulario({
    name: 'Vox AC30',
    price: '1800',
    description: 'Amplificador valvular de 30W icono del sonido britanico',
    category: 'Amplificadores',
    stock: '2',
  })
  await userEvent.click(screen.getByRole('button', { name: /guardar/i }))

  await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/admin'))
})

test('muestra error si la API falla al guardar', async () => {
  client.post.mockRejectedValueOnce(new Error('Server error'))

  renderFormulario()

  await completarFormulario({
    name: 'Vox AC30',
    price: '1800',
    description: 'Amplificador valvular de 30W icono del sonido britanico',
    category: 'Amplificadores',
    stock: '2',
  })
  await userEvent.click(screen.getByRole('button', { name: /guardar/i }))

  await waitFor(() => expect(screen.getByText(/error al crear/i)).toBeInTheDocument())
})
